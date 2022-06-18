package nl.marc_apps.streamer.rtsp.connection_state

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.StreamPorts
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo

class InitialisedConnectionState(
    private val commandsManager: CommandsManager,
    private val connection: Connection,
    private val url: RtspUrlInfo,
    private val videoSourcePorts: StreamPorts,
    private val audioSourcePorts: StreamPorts
) : ConnectionState() {
    override val isConnectionOpen = true

    override val isStreaming = false

    override suspend fun startStreaming(
        actorSelectorManager: ActorSelectorManager,
        protocol: Protocol,
        audioSampleRate: Int?,
        videoMetadata: H26xMetadata?,
        audioTrack: Int,
        videoTrack: Int,
        checkServerAlive: Boolean,
        changeState: (ConnectionState) -> Unit
    ): ConnectionState {
        return StreamingState(commandsManager, connection, url, actorSelectorManager, protocol, audioSampleRate, videoMetadata, videoSourcePorts, audioSourcePorts, audioTrack, videoTrack).also {
            it.emitRtspFrames()
            it.checkHeartbeat(checkServerAlive, changeState)
        }
    }

    override suspend fun disconnect(): ConnectionState {
        closeConnection(connection, commandsManager)
        return NotConnectedState(commandsManager)
    }
}
