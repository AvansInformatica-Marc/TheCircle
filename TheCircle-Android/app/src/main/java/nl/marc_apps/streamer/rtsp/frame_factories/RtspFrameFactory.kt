package nl.marc_apps.streamer.rtsp.frame_factories

import android.media.MediaCodec
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.VideoCodec
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import nl.marc_apps.streamer.rtsp.utils.setLong
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

abstract class RtspFrameFactory(
    private val clock: Long,
    private val payloadType: Int,
    private val frameType: RtspFrame.FrameType
) {
    protected var channelIdentifier: Int = 0
    private var seq = 0L
    private var ssrc = 0L
    protected val maxPacketSize = RtspConstants.MTU - 28

    abstract fun createAndSendPacket(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): List<RtspFrame>

    open fun reset() {
        seq = 0
        ssrc = 0
    }

    fun setSSRC(ssrc: Long) {
        this.ssrc = ssrc
    }

    protected fun getBuffer(size: Int): ByteArray {
        val buffer = ByteArray(size)
        buffer[0] = 0x80.toByte()
        buffer[1] = payloadType.toByte()
        setLongSSRC(buffer, ssrc)
        requestBuffer(buffer)
        return buffer
    }

    protected fun updateTimeStamp(buffer: ByteArray, timestamp: Long): Long {
        val ts = timestamp * clock / 1_000_000_000L
        buffer.setLong(ts, 4 until 8)
        return ts
    }

    protected fun updateSeq(buffer: ByteArray) {
        buffer.setLong(++seq, 2 until 4)
    }

    protected fun markPacket(buffer: ByteArray) {
        buffer[1] = buffer[1] or 0x80.toByte()
    }

    protected fun createRtpFrame(buffer: ByteArray, rtpTimestamp: Long, size: Int = buffer.size): RtspFrame {
        return RtspFrame(buffer, rtpTimestamp, size, channelIdentifier, frameType)
    }

    private fun setLongSSRC(buffer: ByteArray, ssrc: Long) {
        buffer.setLong(ssrc, 8 until 12)
    }

    private fun requestBuffer(buffer: ByteArray) {
        buffer[1] = buffer[1] and 0x7F
    }

    companion object {
        @JvmStatic
        val TAG = "BasePacket"

        fun createVideoRtpFrameFactory(
            metadata: H26xMetadata,
            rtpVideoTrack: Int
        ): RtspFrameFactory {
            return when (metadata.codec) {
                VideoCodec.H264 -> H264RtspFrameFactory(metadata.sequenceParameterSet, metadata.pictureParameterSet, rtpVideoTrack)
                VideoCodec.H265 -> H265RtspFrameFactory(metadata.sequenceParameterSet, metadata.pictureParameterSet, rtpVideoTrack)
            }
        }
    }
}
