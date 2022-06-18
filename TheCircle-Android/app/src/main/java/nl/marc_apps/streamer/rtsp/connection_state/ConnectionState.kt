package nl.marc_apps.streamer.rtsp.connection_state

import android.media.MediaCodec
import android.util.Log
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo
import java.io.IOException
import java.nio.ByteBuffer

abstract class ConnectionState {
    abstract val isConnectionOpen: Boolean

    abstract val isStreaming: Boolean

    open val frameStatistics: FrameStatistics
        get() = FrameStatistics()

    private val disconnectionLock = Mutex()

    private var isDisconnecting = false

    open suspend fun setupConnection(url: RtspUrlInfo, actorSelectorManager: ActorSelectorManager): ConnectionState {
        throw IllegalStateException("Connection setup already in progress or completed")
    }

    open suspend fun authorise(user: String? = null, password: String? = null): ConnectionState {
        throw IllegalStateException("Can't authorise")
    }

    open suspend fun initialiseStreams(streamType: RtspClient.StreamType): ConnectionState {
        throw IllegalStateException("Can't initialise streams")
    }

    open suspend fun startStreaming(
        actorSelectorManager: ActorSelectorManager,
        protocol: Protocol,
        audioSampleRate: Int? = null,
        videoMetadata: H26xMetadata? = null,
        audioTrack: Int,
        videoTrack: Int,
        checkServerAlive: Boolean = false,
        changeState: (ConnectionState) -> Unit
    ): ConnectionState {
        throw IllegalStateException("Can't initialise streams")
    }

    open fun sendVideo(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {}

    open fun sendAudio(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {}

    abstract suspend fun disconnect(): ConnectionState

    protected suspend fun closeConnection(connection: Connection, commandsManager: CommandsManager) {
        disconnectionLock.withLock {
            if (isDisconnecting) return

            isDisconnecting = true
            try {
                val teardown = commandsManager.createTeardown()
                commandsManager.logCommand(teardown)
                connection.output.writeStringUtf8(teardown.toString())
                connection.output.flush()
                connection.socket.awaitClosed()
            } catch (e: IOException) {
                Log.e(TAG, "disconnect error", e)
            }

            commandsManager.clear()
        }
    }

    companion object {
        private const val TAG = "ConnectionState"
    }
}
