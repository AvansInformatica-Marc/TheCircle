package nl.marc_apps.streamer.rtsp

import android.media.MediaCodec
import android.util.Log
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.commands.ResponseManager
import nl.marc_apps.streamer.rtsp.frame_factories.AacRtspFrameFactory
import nl.marc_apps.streamer.rtsp.frame_factories.RtspFrameFactory
import nl.marc_apps.streamer.rtsp.frame_writers.StreamFrameWriter
import nl.marc_apps.streamer.rtsp.state_reports.StreamStateReportWriter
import nl.marc_apps.streamer.rtsp.utils.BitrateManager
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo
import java.nio.ByteBuffer
import java.util.*

open class RtspSender(
    private val actorSelectorManager: ActorSelectorManager,
    protocol: Protocol,
    videoSourcePorts: StreamPorts,
    audioSourcePorts: StreamPorts,
    private val audioTrack: Int,
    private val videoTrack: Int,
    audioSampleRate: Int?,
    videoMetadata: H26xMetadata?,
    connection: Connection,
    private val url: RtspUrlInfo,
    loggingLevel: RtspLoggingLevel,
    cacheSize: Int = DEFAULT_CACHE_SIZE
) {
    private val videoRtspFrameFactory: RtspFrameFactory? = videoMetadata?.let {
        RtspFrameFactory.createVideoRtpFrameFactory(it, videoTrack)
    }

    private val audioRtspFrameFactory: RtspFrameFactory? = audioSampleRate?.let {
        AacRtspFrameFactory(it, audioTrack)
    }

    private val closeables = mutableSetOf<suspend () -> Unit>()

    private fun openUdpWriteChannel(remotePort: Int, localPort: Int): ByteWriteChannel {
        val socket = aSocket(actorSelectorManager).udp().connect(
            InetSocketAddress(url.host, remotePort),
            InetSocketAddress("127.0.0.1", localPort)
        )
        closeables += socket::awaitClosed
        return socket.openWriteChannel(true)
    }

    private val rtpSocket = if (protocol == Protocol.TCP) {
        StreamFrameWriter(connection.output, connection.output,
            sendChannelIdentifier = true,
            loggingLevel = loggingLevel
        )
    } else {
        StreamFrameWriter(
            openUdpWriteChannel(videoSourcePorts.streamPort, ResponseManager.videoClientPorts.streamPort),
            openUdpWriteChannel(audioSourcePorts.streamPort, ResponseManager.audioClientPorts.streamPort),
            sendChannelIdentifier = false,
            loggingLevel = loggingLevel
        )
    }

    private val baseSenderReport = if (protocol == Protocol.TCP) {
        StreamStateReportWriter(connection.output, connection.output,
            sendChannelIdentifier = true,
            loggingLevel = loggingLevel
        )
    } else {
        StreamStateReportWriter(
            openUdpWriteChannel(videoSourcePorts.streamStateReportPort, ResponseManager.videoClientPorts.streamStateReportPort),
            openUdpWriteChannel(audioSourcePorts.streamStateReportPort, ResponseManager.audioClientPorts.streamStateReportPort),
            sendChannelIdentifier = false,
            loggingLevel = loggingLevel
        )
    }

    private var rtpFrameChannel = Channel<RtspFrame>(cacheSize)

    private var job = SupervisorJob()

    private var running = false

    val frameStatistics = FrameStatistics()

    private val bitrateManager = BitrateManager()

    fun sendVideoFrame(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (running) {
            val frames = videoRtspFrameFactory?.createAndSendPacket(h264Buffer, info) ?: emptyList()
            for (frame in frames) {
                onFrameCreated(frame)
            }
        }
    }

    fun sendAudioFrame(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (running) {
            val frames = audioRtspFrameFactory?.createAndSendPacket(aacBuffer, info) ?: emptyList()
            for (frame in frames) {
                onFrameCreated(frame)
            }
        }
    }

    private fun onFrameCreated(rtspFrame: RtspFrame) {
        rtpFrameChannel.trySend(rtspFrame).onFailure {
            Log.i(TAG, "${rtspFrame.frameType.name} frame discarded")
            frameStatistics.onFrameDropped(rtspFrame.frameType)
        }
    }

    private fun setPacketSsrcs() {
        val ssrcVideo = Random().nextInt().toLong()
        val ssrcAudio = Random().nextInt().toLong()
        baseSenderReport.setSSRC(ssrcVideo, ssrcAudio)
        videoRtspFrameFactory?.setSSRC(ssrcVideo)
        audioRtspFrameFactory?.setSSRC(ssrcAudio)
    }

    suspend fun startStreaming() {
        setPacketSsrcs()

        coroutineScope {
            launch(job + newSingleThreadContext("RTP_THREAD")) {
                running = true
                for (rtpFrame in rtpFrameChannel) {
                    ensureActive()
                    rtpSocket.sendFrame(rtpFrame)
                    ensureActive()
                    updateStatsForSentFrame(rtpFrame)
                    ensureActive()
                }
            }
        }
    }

    private suspend fun updateStatsForSentFrame(sentRtspFrame: RtspFrame) {
        val packetSize = sentRtspFrame.length + rtpSocket.additionalPacketSize
        bitrateManager.calculateBitrate(packetSize * 8.toLong())

        frameStatistics.onFrameSent(sentRtspFrame.frameType)

        if (baseSenderReport.update(sentRtspFrame)) {
            val reportSize = StreamStateReportWriter.PACKET_LENGTH + rtpSocket.additionalPacketSize
            bitrateManager.calculateBitrate(reportSize * 8.toLong())
        }
    }

    suspend fun stop() {
        running = false
        rtpFrameChannel.close()
        job.cancelAndJoin()
        for (closable in closeables) {
            closable()
        }
        Log.d(TAG, frameStatistics.toString())
        baseSenderReport.reset()
        audioRtspFrameFactory?.reset()
        videoRtspFrameFactory?.reset()
        frameStatistics.resetStatistics()
    }

    companion object {
        private const val TAG = "RtspSender"

        private const val DEFAULT_CACHE_SIZE = 10 * 1024 * 1024 / RtspConstants.MTU
    }
}
