package nl.marc_apps.streamer.rtsp.connection_state

import android.media.MediaCodec
import io.ktor.network.selector.*
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo
import java.nio.ByteBuffer

class ConnectionStateManager(commandsManager: CommandsManager) {
    private var currentState: ConnectionState = NotConnectedState(commandsManager)

    val isConnectionOpen: Boolean
        get() = currentState.isConnectionOpen

    val isStreaming: Boolean
        get() = currentState.isStreaming

    val frameStatistics: FrameStatistics
        get() = currentState.frameStatistics

    suspend fun setupConnection(url: RtspUrlInfo, actorSelectorManager: ActorSelectorManager) {
        invokeFunctionOnState { setupConnection(url, actorSelectorManager) }
    }

    suspend fun authorise(user: String?, password: String?) {
        invokeFunctionOnState { authorise(user, password) }
    }

    suspend fun initialiseStreams(streamType: RtspClient.StreamType) {
        invokeFunctionOnState { initialiseStreams(streamType) }
    }

    suspend fun startStreaming(
        actorSelectorManager: ActorSelectorManager,
        protocol: Protocol,
        audioTrack: Int,
        audioSampleRate: Int? = null,
        videoMetadata: H26xMetadata? = null,
        videoTrack: Int,
        checkServerAlive: Boolean = false
    ) {
        invokeFunctionOnState {
            startStreaming(actorSelectorManager, protocol, audioSampleRate, videoMetadata, audioTrack, videoTrack, checkServerAlive) {
                currentState = it
            }
        }
    }

    fun sendVideo(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        currentState.sendVideo(h264Buffer, info)
    }

    fun sendAudio(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        currentState.sendAudio(aacBuffer, info)
    }

    suspend fun disconnect() {
        invokeFunctionOnState { disconnect() }
    }

    private suspend fun invokeFunctionOnState(function: suspend ConnectionState.() -> ConnectionState) {
        try {
            currentState = function(currentState)
        } catch (error: Throwable) {
            try {
                currentState.disconnect()
            } catch (ignored: Throwable) { }
            throw error
        }
    }
}
