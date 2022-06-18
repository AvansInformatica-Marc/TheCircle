package nl.marc_apps.streamer.rtsp.commands

import android.util.Log
import io.ktor.utils.io.*
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.RtspLoggingLevel
import nl.marc_apps.streamer.rtsp.StreamPorts

class ResponseManager(
    private val loggingLevel: RtspLoggingLevel = RtspLoggingLevel.BASIC
) {
    internal var sessionId: String? = null

    suspend fun getResponse(readChannel: ByteReadChannel): Response {
        Log.v("RTSP", "<-- Waiting for incoming messages...")
        readChannel.awaitContent()
        val response = readChannel.readPacket(readChannel.availableForRead).readText()
        return getResponse(response)
    }

    private fun getResponse(response: String): Response {
        val parsedResponse = Response.parse(response)
        sessionId = parsedResponse.sessionId ?: sessionId ?: ""
        when (loggingLevel) {
            RtspLoggingLevel.NONE -> {}
            RtspLoggingLevel.BASIC, RtspLoggingLevel.HEADERS -> {
                Log.i("RTSP", "<-- ${response.lineSequence().first()}")
            }
            RtspLoggingLevel.PLAIN_TEXT_BODY, RtspLoggingLevel.FULL -> {
                val lines = response.lineSequence()
                var isFirst = true
                for (line in lines) {
                    if (isFirst) {
                        Log.i("RTSP", "<-- $line")
                        isFirst = false
                    } else {
                        Log.d("RTSP", line)
                    }
                }
                Log.i("RTSP", "<-- END RTSP")
            }
        }
        return parsedResponse
    }

    companion object {
        val audioClientPorts = StreamPorts(5000, type = RtspFrame.FrameType.AUDIO)
        val videoClientPorts = StreamPorts(5002, type = RtspFrame.FrameType.VIDEO)
        val audioServerPorts = StreamPorts(5004, type = RtspFrame.FrameType.AUDIO)
        val videoServerPorts = StreamPorts(5006, type = RtspFrame.FrameType.VIDEO)
    }
}
