package nl.marc_apps.streamer.rtsp.connection_state

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.withContext
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.StreamPorts
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.commands.Response
import nl.marc_apps.streamer.rtsp.commands.ResponseManager
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo

class UninitialisedConnectionState(
    private val commandsManager: CommandsManager,
    private val connection: Connection,
    private val url: RtspUrlInfo
) : ConnectionState() {
    override val isConnectionOpen = true

    override val isStreaming = false

    private val job = SupervisorJob()

    private fun getServerPorts(response: Response, frameType: RtspFrame.FrameType): StreamPorts {
        val defaultPorts = if (frameType == RtspFrame.FrameType.AUDIO) {
            ResponseManager.audioServerPorts
        } else {
            ResponseManager.videoServerPorts
        }

        return defaultPorts.copy(
            streamPort = response.serverPorts?.first ?: defaultPorts.streamPort,
            streamStateReportPort = response.serverPorts?.second ?: defaultPorts.streamStateReportPort
        )
    }

    override suspend fun initialiseStreams(streamType: RtspClient.StreamType): ConnectionState {
        return withContext(job) {
            var videoPorts = ResponseManager.videoServerPorts
            var audioPorts = ResponseManager.audioServerPorts

            if (streamType != RtspClient.StreamType.AUDIO_ONLY) {
                val setupVideoCommand = commandsManager.createSetup(RtspFrame.FrameType.VIDEO)
                commandsManager.logCommand(setupVideoCommand)
                connection.output.writeStringUtf8(setupVideoCommand.toString())
                connection.output.flush()

                val setupResponse = commandsManager.getResponse(connection.input)
                val setupVideoStatus = setupResponse.statusCode
                if (setupVideoStatus != STATUS_SUCCESS) {
                    throw IllegalStateException("setup video $setupVideoStatus")
                }
                videoPorts = getServerPorts(setupResponse, RtspFrame.FrameType.VIDEO)
            }

            if (streamType != RtspClient.StreamType.VIDEO_ONLY) {
                val setupAudioCommand = commandsManager.createSetup(RtspFrame.FrameType.AUDIO)
                commandsManager.logCommand(setupAudioCommand)
                connection.output.writeStringUtf8(setupAudioCommand.toString())
                connection.output.flush()

                val setupResponse = commandsManager.getResponse(connection.input)
                val setupAudioStatus = setupResponse.statusCode
                if (setupAudioStatus != STATUS_SUCCESS) {
                    throw IllegalStateException("setup audio $setupAudioStatus")
                }
                audioPorts = getServerPorts(setupResponse, RtspFrame.FrameType.AUDIO)
            }

            val recordCommand = commandsManager.createRecord()
            commandsManager.logCommand(recordCommand)
            connection.output.writeStringUtf8(recordCommand.toString())
            connection.output.flush()

            val recordStatus = commandsManager.getResponse(connection.input).statusCode
            if (recordStatus != STATUS_SUCCESS) {
                throw IllegalStateException("record $recordStatus")
            }

            InitialisedConnectionState(commandsManager, connection, url, videoPorts, audioPorts)
        }
    }

    override suspend fun disconnect(): ConnectionState {
        job.cancelAndJoin()
        closeConnection(connection, commandsManager)
        return NotConnectedState(commandsManager)
    }

    companion object {
        private const val STATUS_SUCCESS = 200
    }
}
