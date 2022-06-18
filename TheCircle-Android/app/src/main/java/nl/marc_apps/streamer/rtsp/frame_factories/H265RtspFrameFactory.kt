package nl.marc_apps.streamer.rtsp.frame_factories

import android.media.MediaCodec
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import java.nio.ByteBuffer
import kotlin.experimental.and

open class H265RtspFrameFactory(
    sequenceParameterSet: ByteArray,
    pictureParameterSet: ByteArray,
    rtpVideoTrack: Int
) : RtspFrameFactory(RtspConstants.clockVideoFrequency, RtspConstants.payloadType + rtpVideoTrack, RtspFrame.FrameType.VIDEO) {
    private val header = ByteArray(6)
    private val stapA = ByteArray(sequenceParameterSet.size + pictureParameterSet.size + 6)
    private var sendKeyFrame = false

    init {
        channelIdentifier = rtpVideoTrack
        setParameterSets(sequenceParameterSet, pictureParameterSet)
    }

    override fun createAndSendPacket(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): List<RtspFrame> {
        var frames = mutableListOf<RtspFrame>()

        // We read a NAL units from ByteBuffer and we send them
        // NAL units are preceded with 0x00000001
        byteBuffer.rewind()
        byteBuffer.get(header, 0, 6)
        val ts = bufferInfo.presentationTimeUs * 1000L
        val naluLength = bufferInfo.size - byteBuffer.position()
        val type: Int = header[4].toInt().shr(1 and 0x3f)
        if (type == RtspConstants.IDR_N_LP || type == RtspConstants.IDR_W_DLP || bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
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
            if (naluLength <= maxPacketSize - RtspConstants.RTP_HEADER_LENGTH - 2) {
                val buffer = getBuffer(naluLength + RtspConstants.RTP_HEADER_LENGTH + 2)
                //Set PayloadHdr (exact copy of nal unit header)
                buffer[RtspConstants.RTP_HEADER_LENGTH] = header[4]
                buffer[RtspConstants.RTP_HEADER_LENGTH + 1] = header[5]
                byteBuffer.get(buffer, RtspConstants.RTP_HEADER_LENGTH + 2, naluLength)
                val rtpTimestamp = updateTimeStamp(buffer, ts)
                markPacket(buffer) //mark end frame
                updateSeq(buffer)
                val rtpFrame = createRtpFrame(buffer, rtpTimestamp)
                frames += rtpFrame
            } else {
                //Set PayloadHdr (16bit type=49)
                header[0] = (49 shl 1).toByte()
                header[1] = 1
                // Set FU header
                //   +---------------+
                //   |0|1|2|3|4|5|6|7|
                //   +-+-+-+-+-+-+-+-+
                //   |S|E|  FuType   |
                //   +---------------+
                header[2] = type.toByte() // FU header type
                header[2] = (header[2] + 0x80).toByte() // Start bit
                var sum = 0
                while (sum < naluLength) {
                    val length = if (naluLength - sum > maxPacketSize - RtspConstants.RTP_HEADER_LENGTH - 3) {
                        maxPacketSize - RtspConstants.RTP_HEADER_LENGTH - 3
                    } else {
                        bufferInfo.size - byteBuffer.position()
                    }
                    val buffer = getBuffer(length + RtspConstants.RTP_HEADER_LENGTH + 3)
                    buffer[RtspConstants.RTP_HEADER_LENGTH] = header[0]
                    buffer[RtspConstants.RTP_HEADER_LENGTH + 1] = header[1]
                    buffer[RtspConstants.RTP_HEADER_LENGTH + 2] = header[2]
                    val rtpTs = updateTimeStamp(buffer, ts)
                    byteBuffer.get(buffer, RtspConstants.RTP_HEADER_LENGTH + 3, length)
                    sum += length
                    // Last packet before next NAL
                    if (sum >= naluLength) {
                        // End bit on
                        buffer[RtspConstants.RTP_HEADER_LENGTH + 2] = (buffer[RtspConstants.RTP_HEADER_LENGTH + 2] + 0x40).toByte()
                        markPacket(buffer) //mark end frame
                    }
                    updateSeq(buffer)
                    val rtpFrame = createRtpFrame(buffer, rtpTs)
                    frames += rtpFrame
                    // Switch start bit
                    header[2] = header[2] and 0x7F
                }
            }
        }

        return frames
    }

    private fun setParameterSets(sequenceParameterSet: ByteArray, pictureParameterSet: ByteArray) {
        stapA[0] = (48 shl 1).toByte()
        stapA[1] = 1

        // Write NALU 1 size into the array (NALU 1 is the SPS).
        stapA[2] = (sequenceParameterSet.size shr 8).toByte()
        stapA[3] = (sequenceParameterSet.size and 0xFF).toByte()

        // Write NALU 2 size into the array (NALU 2 is the PPS).
        stapA[sequenceParameterSet.size + 4] = (pictureParameterSet.size shr 8).toByte()
        stapA[sequenceParameterSet.size + 5] = (pictureParameterSet.size and 0xFF).toByte()

        // Write NALU 1 into the array, then write NALU 2 into the array.
        System.arraycopy(sequenceParameterSet, 0, stapA, 4, sequenceParameterSet.size)
        System.arraycopy(pictureParameterSet, 0, stapA, 6 + sequenceParameterSet.size, pictureParameterSet.size)
    }

    override fun reset() {
        super.reset()
        sendKeyFrame = false
    }
}
