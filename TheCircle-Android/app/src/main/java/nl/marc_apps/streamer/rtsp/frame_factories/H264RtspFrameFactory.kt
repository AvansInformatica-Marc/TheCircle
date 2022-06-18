package nl.marc_apps.streamer.rtsp.frame_factories

import android.media.MediaCodec
import android.util.Log
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import nl.marc_apps.streamer.rtsp.utils.getVideoStartCodeSize
import java.nio.ByteBuffer
import kotlin.experimental.and

open class H264RtspFrameFactory(
    private val sequenceParameterSet: ByteArray,
    private val pictureParameterSet: ByteArray,
    rtpVideoTrack: Int
) : RtspFrameFactory(RtspConstants.clockVideoFrequency, RtspConstants.payloadType + rtpVideoTrack, RtspFrame.FrameType.VIDEO) {
    private lateinit var stapA: ByteArray
    private var sendKeyFrame = false

    init {
        channelIdentifier = rtpVideoTrack
        setParameterSets()
    }

    override fun createAndSendPacket(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): List<RtspFrame> {
        var frames = mutableListOf<RtspFrame>()

        // We read a NAL units from ByteBuffer and we send them
        // NAL units are preceded with 0x00000001
        byteBuffer.rewind()
        val header = ByteArray(getHeaderSize(byteBuffer) + 1)
        if (header.size == 1) {
            //invalid buffer or waiting for sps/pps
            return frames
        }
        byteBuffer.rewind()
        byteBuffer.get(header, 0, header.size)
        val ts = bufferInfo.presentationTimeUs * 1000L
        val naluLength = bufferInfo.size - byteBuffer.position()
        val type: Int = (header[header.size - 1] and 0x1F).toInt()
        if (type == RtspConstants.IDR || bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
            val buffer = getBuffer(stapA.size + RtspConstants.RTP_HEADER_LENGTH)
            val rtpTimestamp = updateTimeStamp(buffer, ts)
            markPacket(buffer) //mark end frame
            System.arraycopy(stapA, 0, buffer, RtspConstants.RTP_HEADER_LENGTH, stapA.size)
            updateSeq(buffer)
            val rtpFrame = createRtpFrame(buffer, rtpTimestamp, stapA.size + RtspConstants.RTP_HEADER_LENGTH)
            frames += rtpFrame
            sendKeyFrame = true
        }

        if (sendKeyFrame) {
            // Small NAL unit => Single NAL unit
            if (naluLength <= maxPacketSize - RtspConstants.RTP_HEADER_LENGTH - 1) {
                val buffer = getBuffer(naluLength + RtspConstants.RTP_HEADER_LENGTH + 1)
                buffer[RtspConstants.RTP_HEADER_LENGTH] = header[header.size - 1]
                byteBuffer.get(buffer, RtspConstants.RTP_HEADER_LENGTH + 1, naluLength)
                val rtpTimestamp = updateTimeStamp(buffer, ts)
                markPacket(buffer) //mark end frame
                updateSeq(buffer)
                val rtpFrame = createRtpFrame(buffer, rtpTimestamp)
                frames += rtpFrame
            } else {
                // Set FU-A header
                header[1] = header[header.size - 1] and 0x1F // FU header type
                header[1] = header[1].plus(0x80).toByte()  // set start bit to 1
                // Set FU-A indicator
                header[0] = header[header.size - 1] and 0x60 and 0xFF.toByte() // FU indicator NRI
                header[0] = header[0].plus(28).toByte()
                var sum = 0
                while (sum < naluLength) {
                    val length = if (naluLength - sum > maxPacketSize - RtspConstants.RTP_HEADER_LENGTH - 2) {
                        maxPacketSize - RtspConstants.RTP_HEADER_LENGTH - 2
                    } else {
                        bufferInfo.size - byteBuffer.position()
                    }
                    val buffer = getBuffer(length + RtspConstants.RTP_HEADER_LENGTH + 2)
                    buffer[RtspConstants.RTP_HEADER_LENGTH] = header[0]
                    buffer[RtspConstants.RTP_HEADER_LENGTH + 1] = header[1]
                    val rtpTs = updateTimeStamp(buffer, ts)
                    byteBuffer.get(buffer, RtspConstants.RTP_HEADER_LENGTH + 2, length)
                    sum += length
                    // Last packet before next NAL
                    if (sum >= naluLength) {
                        // End bit on
                        buffer[RtspConstants.RTP_HEADER_LENGTH + 1] = buffer[RtspConstants.RTP_HEADER_LENGTH + 1].plus(0x40).toByte()
                        markPacket(buffer) //mark end frame
                    }
                    updateSeq(buffer)
                    val rtpFrame = createRtpFrame(buffer, rtpTs)
                    frames += rtpFrame
                    // Switch start bit
                    header[1] = header[1] and 0x7F
                }
            }
        } else {
            Log.v(TAG, "waiting for keyframe")
        }

        return frames
    }

    private fun setParameterSets() {
        stapA = ByteArray(sequenceParameterSet.size + pictureParameterSet.size + 5).also {
            // STAP-A NAL header is 24
            it[0] = 24

            // Write NALU 1 size into the array (NALU 1 is the SPS).
            it[1] = (sequenceParameterSet.size shr 8).toByte()
            it[2] = (sequenceParameterSet.size and 0xFF).toByte()

            // Write NALU 2 size into the array (NALU 2 is the PPS).
            it[sequenceParameterSet.size + 3] = (pictureParameterSet.size shr 8).toByte()
            it[sequenceParameterSet.size + 4] = (pictureParameterSet.size and 0xFF).toByte()

            // Write NALU 1 into the array, then write NALU 2 into the array.
            System.arraycopy(sequenceParameterSet, 0, it, 3, sequenceParameterSet.size)
            System.arraycopy(pictureParameterSet, 0, it, 5 + sequenceParameterSet.size, pictureParameterSet.size)
        }
    }

    private fun getHeaderSize(byteBuffer: ByteBuffer): Int {
        if (byteBuffer.remaining() < 4) {
            return 0
        }

        val startCodeSize = byteBuffer.getVideoStartCodeSize()
        if (startCodeSize == 0) {
            return 0
        }
        val startCode = ByteArray(startCodeSize) { 0x00 }
        startCode[startCodeSize - 1] = 0x01
        val avcHeader = startCode + sequenceParameterSet + startCode + pictureParameterSet + startCode
        if (byteBuffer.remaining() < avcHeader.size) {
            return startCodeSize
        }

        val possibleAvcHeader = ByteArray(avcHeader.size)
        byteBuffer.get(possibleAvcHeader, 0, possibleAvcHeader.size)
        return if (avcHeader contentEquals possibleAvcHeader) {
            avcHeader.size
        } else {
            startCodeSize
        }
    }

    override fun reset() {
        super.reset()
        sendKeyFrame = false
    }
}
