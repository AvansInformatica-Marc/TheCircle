package nl.marc_apps.streamer.rtsp.frame_writers

import android.util.Log
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.RtspLoggingLevel

class StreamFrameWriter(
    private val videoWriteChannel: ByteWriteChannel,
    private val audioWriteChannel: ByteWriteChannel,
    private val sendChannelIdentifier: Boolean,
    private val loggingLevel: RtspLoggingLevel
) {
    val additionalPacketSize: Int = if(sendChannelIdentifier) 4 else 0

    suspend fun sendFrame(rtspFrame: RtspFrame) {
        val channelIdentifierHeader = createChannelIdentifierHeader(rtspFrame.length, rtspFrame.channelIdentifier)

        val channel = when (rtspFrame.frameType) {
            RtspFrame.FrameType.VIDEO -> videoWriteChannel
            RtspFrame.FrameType.AUDIO -> audioWriteChannel
        }
        if (sendChannelIdentifier) {
            channel.writePacket(ByteReadPacket(channelIdentifierHeader))
        }
        channel.writePacket(ByteReadPacket(rtspFrame.buffer, 0, rtspFrame.length))
        channel.flush()

        when(loggingLevel) {
            RtspLoggingLevel.PLAIN_TEXT_BODY -> {
                Log.i("RTP", "--> RTP CHANNEL ${2 * rtspFrame.channelIdentifier} (${rtspFrame.frameType.name}; ${rtspFrame.length + additionalPacketSize} bytes)")
            }
            RtspLoggingLevel.FULL -> {
                Log.i("RTP", "--> RTP CHANNEL ${2 * rtspFrame.channelIdentifier} (${rtspFrame.frameType.name}; ${rtspFrame.length + additionalPacketSize} bytes)")
                Log.v("RTP", channelIdentifierHeader.encodeBase64())
                Log.v("RTP", rtspFrame.buffer.encodeBase64())
                Log.i("RTP", "--> END RTP")
            }
            else -> {}
        }
    }

    companion object {
        const val TAG = "StreamFrameWriter"

        private fun createChannelIdentifierHeader(frameLength: Int, channelIdentifier: Int): ByteArray {
            return byteArrayOf(
                '$'.code.toByte(),
                (2 * channelIdentifier).toByte(),
                (frameLength shr 8).toByte(),
                (frameLength and 0xFF).toByte()
            )
        }
    }
}
