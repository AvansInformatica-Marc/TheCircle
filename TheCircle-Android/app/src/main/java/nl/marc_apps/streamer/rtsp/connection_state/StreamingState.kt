package nl.marc_apps.streamer.rtsp.connection_state

import android.media.MediaCodec
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.RtspSender
import nl.marc_apps.streamer.rtsp.StreamPorts
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import kotlin.time.Duration.Companion.seconds

class StreamingState(
    private val commandsManager: CommandsManager,
    private val connection: Connection,
    url: RtspUrlInfo,
    actorSelectorManager: ActorSelectorManager,
    protocol: Protocol,
    audioSampleRate: Int?,
    videoMetadata: H26xMetadata?,
    videoSourcePorts: StreamPorts,
    audioSourcePorts: StreamPorts,
    audioTrack: Int,
    videoTrack: Int
) : ConnectionState() {
    override val isConnectionOpen = true

    override val isStreaming = true

    private val job = SupervisorJob()

    private val rtspSender = RtspSender(
        actorSelectorManager,
        protocol,
        videoSourcePorts,
        audioSourcePorts,
        audioTrack,
        videoTrack,
        audioSampleRate,
        videoMetadata,
        connection,
        url,
        commandsManager.loggingLevel
    )

    override val frameStatistics: FrameStatistics
        get() = rtspSender.frameStatistics

    suspend fun emitRtspFrames() {
        rtspSender.startStreaming()
    }

    override fun sendVideo(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspSender.sendVideoFrame(h264Buffer, info)
    }

    override fun sendAudio(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspSender.sendAudioFrame(aacBuffer, info)
    }

    suspend fun checkHeartbeat(checkServerAlive: Boolean = false, changeState: (ConnectionState) -> Unit) {
        coroutineScope {
            launch(job + newSingleThreadContext("HEARTBEAT_THREAD")) {
                var isNotAlive = false
                while(isActive && isStreaming) {
                    try {
                        if (isAlive(checkServerAlive)) {
                            ensureActive()
                            delay(2000)
                            ensureActive()

                            if (connection.input.availableForRead > 0) {
                                commandsManager.getResponse(connection.input)
                            }
                        } else {
                            isNotAlive = true
                            break
                        }
                    } catch (ignored: SocketTimeoutException) {
                        // new packet not found
                    }
                }

                if (isNotAlive) {
                    changeState(disconnect())
                }
            }
        }
    }

    private suspend fun isAlive(checkServerAlive: Boolean = false): Boolean {
        val connected = !connection.socket.isClosed
        val reachable = !checkServerAlive || isReachable()
        return connected && reachable
    }

    private suspend fun isReachable(): Boolean {
        val address = connection.socket.remoteAddress.toJavaAddress() as? InetSocketAddress
        return withContext(Dispatchers.IO) {
            address?.address?.isReachable(5.seconds.inWholeMilliseconds.toInt()) ?: true
        }
    }

    override suspend fun disconnect(): ConnectionState {
        rtspSender.stop()
        job.cancelAndJoin()
        closeConnection(connection, commandsManager)
        return NotConnectedState(commandsManager)
    }
}
