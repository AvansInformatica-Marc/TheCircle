package nl.marc_apps.streamer.rtsp

import android.media.MediaCodec
import io.ktor.network.selector.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.commands.CommandsManager
import nl.marc_apps.streamer.rtsp.commands.ResponseManager
import nl.marc_apps.streamer.rtsp.connection_state.ConnectionStateManager
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo
import java.io.Closeable
import java.nio.ByteBuffer

open class RtspClient internal constructor(
    private val url: RtspUrlInfo,
    private val streamType: StreamType,
    private val protocol: Protocol,
    loggingLevel: RtspLoggingLevel
) : Closeable {
    private val selectorManager = ActorSelectorManager(Dispatchers.IO)

    private val audioTrack = if (streamType == StreamType.AUDIO_ONLY) 0 else 1
    private val videoTrack = if (streamType == StreamType.AUDIO_ONLY) 1 else 0

    private val responseManager = ResponseManager(loggingLevel)
    private val commandsManager = CommandsManager(url, responseManager, streamType, protocol, audioTrack, videoTrack, loggingLevel)

    private val connection = ConnectionStateManager(commandsManager)

    var user: String? = null
        private set

    private var password: String? = null

    val frameStatistics: FrameStatistics
        get() = connection.frameStatistics

    val isConnected get() = connection.isConnectionOpen

    val isStreaming get() = connection.isStreaming

    fun setAuthorization(user: String?, password: String?) {
        this.user = user
        this.password = password
    }

    fun setVideoInfo(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
        commandsManager.setVideoInfo(sps, pps, vps)
    }

    fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {
        commandsManager.setAudioInfo(sampleRate, isStereo)
    }

    private suspend fun loadVideoMetadata(): H26xMetadata? {
        if (streamType != StreamType.AUDIO_ONLY) {
            if (commandsManager.metadata == null) {
                withTimeout(5000) {
                    // TODO: Use a suspending Channel or Flow for this
                    while (isActive && (commandsManager.metadata == null)) {
                        delay(100)
                    }
                }
            }

            return commandsManager.metadata ?: throw IllegalStateException("sps or pps is null")
        }

        return null
    }

    suspend fun connect() {
        if (connection.isConnectionOpen) {
            return
        }

        connection.setupConnection(url, selectorManager)
        loadVideoMetadata()
        connection.authorise(user, password)
        connection.initialiseStreams(streamType)

        val sampleRate = if (streamType != StreamType.VIDEO_ONLY) {
            commandsManager.sampleRate
        } else {
            null
        }

        connection.startStreaming(
            selectorManager, protocol, audioTrack, sampleRate, commandsManager.metadata, videoTrack
        )
    }

    suspend fun disconnect() {
        connection.disconnect()
    }

    suspend fun closeAwait() {
        disconnect()
        close()
    }

    override fun close() {
        selectorManager.close()
    }

    fun sendVideo(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (streamType != StreamType.AUDIO_ONLY) {
            connection.sendVideo(h264Buffer, info)
        }
    }

    fun sendAudio(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (streamType != StreamType.VIDEO_ONLY) {
            connection.sendAudio(aacBuffer, info)
        }
    }

    enum class StreamType {
        AUDIO_ONLY, VIDEO_ONLY, BOTH
    }

    data class Options(
        var streamType: StreamType = StreamType.BOTH,
        var protocol: Protocol = Protocol.TCP,
        var loggingLevel: RtspLoggingLevel = RtspLoggingLevel.BASIC
    )

    companion object {
        fun create(url: String, block: (Options.() -> Unit)? = null): RtspClient {
            val options = Options()
            block?.invoke(options)
            return RtspClient(RtspUrlInfo.parseUrl(url), options.streamType, options.protocol, options.loggingLevel)
        }
    }
}
