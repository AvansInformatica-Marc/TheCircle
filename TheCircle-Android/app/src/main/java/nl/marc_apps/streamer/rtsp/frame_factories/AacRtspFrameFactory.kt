package nl.marc_apps.streamer.rtsp.frame_factories

import android.media.MediaCodec
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

open class AacRtspFrameFactory(
    sampleRate: Int,
    rtpAudioTrack: Int
) : RtspFrameFactory(sampleRate.toLong(), RtspConstants.payloadType + rtpAudioTrack, RtspFrame.FrameType.AUDIO) {

    init {
        channelIdentifier = rtpAudioTrack
    }

    override fun createAndSendPacket(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): List<RtspFrame> {
        var frames = mutableListOf<RtspFrame>()

        val length = bufferInfo.size - byteBuffer.position()
        if (length > 0) {
            val buffer = getBuffer(length + RtspConstants.RTP_HEADER_LENGTH + 4)
            byteBuffer.get(buffer, RtspConstants.RTP_HEADER_LENGTH + 4, length)
            val ts = bufferInfo.presentationTimeUs * 1000
            markPacket(buffer)
            val rtpTs = updateTimeStamp(buffer, ts)

            // AU-headers-length field: contains the size in bits of a AU-header
            // 13+3 = 16 bits -> 13bits for AU-size and 3bits for AU-Index / AU-Index-delta
            // 13 bits will be enough because ADTS uses 13 bits for frame length
            buffer[RtspConstants.RTP_HEADER_LENGTH] = 0.toByte()
            buffer[RtspConstants.RTP_HEADER_LENGTH + 1] = 0x10.toByte()

            // AU-size
            buffer[RtspConstants.RTP_HEADER_LENGTH + 2] = (length shr 5).toByte()
            buffer[RtspConstants.RTP_HEADER_LENGTH + 3] = (length shl 3).toByte()

            // AU-Index
            buffer[RtspConstants.RTP_HEADER_LENGTH + 3] = buffer[RtspConstants.RTP_HEADER_LENGTH + 3] and 0xF8.toByte()
            buffer[RtspConstants.RTP_HEADER_LENGTH + 3] = buffer[RtspConstants.RTP_HEADER_LENGTH + 3] or 0x00
            updateSeq(buffer)

            val rtpFrame = createRtpFrame(buffer, rtpTs, RtspConstants.RTP_HEADER_LENGTH + length + 4)
            frames += rtpFrame
        }

        return frames
    }
}
