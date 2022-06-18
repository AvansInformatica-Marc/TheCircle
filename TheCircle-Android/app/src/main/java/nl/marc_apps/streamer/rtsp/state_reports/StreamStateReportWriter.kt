package nl.marc_apps.streamer.rtsp.state_reports

import android.util.Log
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.RtspLoggingLevel
import kotlin.time.Duration.Companion.seconds

class StreamStateReportWriter(
    private val videoWriteChannel: ByteWriteChannel,
    private val audioWriteChannel: ByteWriteChannel,
    private val sendChannelIdentifier: Boolean,
    private val loggingLevel: RtspLoggingLevel
) {
    val additionalPacketSize: Int = if(sendChannelIdentifier) 4 else 0

    private val interval = 3.seconds

    private val videoStreamStateReport = StreamStateReport()

    private val audioStreamStateReport = StreamStateReport()

    private var videoTime = 0L

    private var audioTime = 0L

    fun setSSRC(ssrcVideo: Long, ssrcAudio: Long) {
        videoStreamStateReport.ssrc = ssrcVideo
        audioStreamStateReport.ssrc = ssrcAudio
    }

    suspend fun sendReport(stateReport: StreamStateReport, rtspFrame: RtspFrame) {
        val channelIdentifierHeader = createChannelIdentifierHeader(rtspFrame.channelIdentifier)
        val streamStateReportData = stateReport.createStreamStateReportData(rtspFrame.timeStamp)
        val buffer = stateReport.writeToBuffer(streamStateReportData)

        val channel = when (rtspFrame.frameType) {
            RtspFrame.FrameType.VIDEO -> videoWriteChannel
            RtspFrame.FrameType.AUDIO -> audioWriteChannel
        }
        if (sendChannelIdentifier) {
            channel.writePacket(ByteReadPacket(channelIdentifierHeader))
        }
        channel.writePacket(ByteReadPacket(buffer, 0, PACKET_LENGTH))
        channel.flush()

        when(loggingLevel) {
            RtspLoggingLevel.PLAIN_TEXT_BODY -> {
                Log.i("RTCP", "--> RTCP CHANNEL ${2 * rtspFrame.channelIdentifier + 1} (${rtspFrame.frameType.name}; ${PACKET_LENGTH + additionalPacketSize} bytes)")
                Log.d("RTCP", streamStateReportData.toString())
                Log.i("RTCP", "--> END RTCP")
            }
            RtspLoggingLevel.FULL -> {
                Log.i("RTCP", "--> RTCP CHANNEL ${2 * rtspFrame.channelIdentifier + 1} (${rtspFrame.frameType.name}; ${PACKET_LENGTH + additionalPacketSize} bytes)")
                Log.v("RTCP", channelIdentifierHeader.encodeBase64())
                Log.v("RTCP", buffer.encodeBase64())
                Log.i("RTCP", "--> END RTCP")
            }
            else -> {}
        }
    }

    suspend fun update(rtspFrame: RtspFrame): Boolean {
        return if (rtspFrame.frameType == RtspFrame.FrameType.VIDEO) {
            updateVideo(rtspFrame)
        } else {
            updateAudio(rtspFrame)
        }
    }

    private suspend fun updateVideo(rtspFrame: RtspFrame): Boolean {
        videoStreamStateReport.packetCount++
        videoStreamStateReport.octetCount += rtspFrame.length
        if (System.currentTimeMillis() - videoTime >= interval.inWholeMilliseconds) {
            videoTime = System.currentTimeMillis()
            sendReport(videoStreamStateReport, rtspFrame)
            return true
        }
        return false
    }

    private suspend fun updateAudio(rtspFrame: RtspFrame): Boolean {
        audioStreamStateReport.packetCount++
        audioStreamStateReport.octetCount += rtspFrame.length
        if (System.currentTimeMillis() - audioTime >= interval.inWholeMilliseconds) {
            audioTime = System.currentTimeMillis()
            sendReport(audioStreamStateReport, rtspFrame)
            return true
        }
        return false
    }

    fun reset() {
        audioTime = 0
        videoTime = 0
    }

    companion object {
        const val PACKET_LENGTH = 28

        const val TAG = "StreamStateReportWriter"

        private fun createChannelIdentifierHeader(channelIdentifier: Int): ByteArray {
            return byteArrayOf(
                '$'.code.toByte(),
                (2 * channelIdentifier + 1).toByte(),
                0,
                PACKET_LENGTH.toByte()
            )
        }
    }
}
