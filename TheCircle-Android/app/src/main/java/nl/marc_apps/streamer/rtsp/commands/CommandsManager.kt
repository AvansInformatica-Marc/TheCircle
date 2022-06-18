package nl.marc_apps.streamer.rtsp.commands

import android.util.Base64
import android.util.Log
import io.ktor.utils.io.*
import nl.marc_apps.streamer.codecs.H264Metadata
import nl.marc_apps.streamer.codecs.H265Metadata
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.RtspFrame
import nl.marc_apps.streamer.rtsp.RtspLoggingLevel
import nl.marc_apps.streamer.rtsp.commands.SdpBody.createAacBody
import nl.marc_apps.streamer.rtsp.commands.SdpBody.createVideoBody
import nl.marc_apps.streamer.rtsp.utils.AuthUtil.getMd5Hash
import nl.marc_apps.streamer.rtsp.utils.RtspUrlInfo
import nl.marc_apps.streamer.rtsp.utils.getVideoStartCodeSize
import nl.marc_apps.streamer.rtsp.utils.ntpSeconds
import nl.marc_apps.streamer.sdp.ConnectionInfo
import nl.marc_apps.streamer.sdp.NetworkAddress
import nl.marc_apps.streamer.sdp.SdpOriginator
import nl.marc_apps.streamer.sdp.SessionDescriptionMessage
import java.nio.ByteBuffer
import java.util.*
import java.util.regex.Pattern

class CommandsManager(
    private val url: RtspUrlInfo,
    private val responseManager: ResponseManager,
    private val streamType: RtspClient.StreamType,
    private val protocol: Protocol,
    private val audioTrack: Int,
    private val videoTrack: Int,
    val loggingLevel: RtspLoggingLevel
) {
    var metadata: H26xMetadata? = null
        private set

    private var cSeq = 0

    private val timeStamp by lazy {
        Date().ntpSeconds
    }

    var sampleRate = 32000
    var isStereo = true

    private var authorization: String? = null

    private fun getData(byteBuffer: ByteBuffer): ByteArray {
        val startCodeSize = byteBuffer.getVideoStartCodeSize()
        val bytes = ByteArray(byteBuffer.capacity() - startCodeSize)
        byteBuffer.position(startCodeSize)
        byteBuffer.get(bytes, 0, bytes.size)
        return bytes
    }

    fun setVideoInfo(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
        val sequenceParameterSet = getData(sps)
        val pictureParameterSet = getData(pps)

        metadata = if (vps != null) {
            H265Metadata(sequenceParameterSet, pictureParameterSet, getData(vps))
        } else {
            H264Metadata(sequenceParameterSet, pictureParameterSet)
        }
    }

    fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {
        this.isStereo = isStereo
        this.sampleRate = sampleRate
    }

    fun clear() {
        metadata = null
        cSeq = 0
        responseManager.sessionId = null
    }

    private fun getDefaultHeaders(): Map<String, String> {
        val headers = mutableMapOf(
            "User-Agent" to "streamer 1.0"
        )

        responseManager.sessionId?.let {
            headers += "Session" to it
        }

        authorization?.let {
            headers += "Authorization" to it
        }

        return headers
    }

    private fun createBody(): SessionDescriptionMessage {
        val videoBody = when (streamType) {
            RtspClient.StreamType.AUDIO_ONLY -> null
            else -> metadata?.let { createVideoBody(videoTrack, it) }
        }

        val audioBody = when (streamType) {
            RtspClient.StreamType.VIDEO_ONLY -> null
            else -> createAacBody(audioTrack, sampleRate, isStereo)
        }

        if (responseManager.sessionId == null) {
            responseManager.sessionId = "$timeStamp"
        }

        return SessionDescriptionMessage(
            originatorAndSessionIdentifier = SdpOriginator(
                sessionId = "$timeStamp",
                versionNumber = timeStamp.toLong(),
                networkAddress = NetworkAddress.Ipv4NetworkAddress("127.0.0.1")
            ),
            connectionInfo = ConnectionInfo(NetworkAddress.Ipv4NetworkAddress(url.host)),
            sessionAttributes = listOf("recvonly"),
            mediaDescriptions = listOfNotNull(videoBody, audioBody)
        )
    }

    private fun createAuth(authHeaders: Map<String, String>, user: String, password: String): String {
        val authPattern = Pattern.compile("realm=\"(.+)\",\\s+nonce=\"(\\w+)\"", Pattern.CASE_INSENSITIVE)
        val matcher = authHeaders.firstNotNullOfOrNull { (key, value) ->
            val matcher = authPattern.matcher(value)
            if (matcher.find()) matcher else null
        }

        Log.i(TAG, "Using ${if(matcher == null) "digest" else "basic"} auth")

        return if (matcher != null) {
            val realm = matcher.group(1)
            val nonce = matcher.group(2)
            val hash1 = getMd5Hash("$user:$realm:$password")
            val hash2 = getMd5Hash("ANNOUNCE:${url.rtspUrl}")
            val hash3 = getMd5Hash("$hash1:$nonce:$hash2")
            "Digest username=\"$user\", realm=\"$realm\", nonce=\"$nonce\", uri=\"${url.rtspUrl}\", response=\"$hash3\""
        } else {
            val base64Data = Base64.encodeToString("$user:$password".toByteArray(), Base64.DEFAULT)
            "Basic $base64Data"
        }
    }

    fun createOptions(): RtspMessage {
        return RtspMessage(RtspMethod.OPTIONS, url.rtspUrl, "RTSP/1.0", ++cSeq, getDefaultHeaders())
    }

    fun createSetup(frameType: RtspFrame.FrameType): RtspMessage {
        val track = when (frameType) {
            RtspFrame.FrameType.VIDEO -> videoTrack
            RtspFrame.FrameType.AUDIO -> audioTrack
        }

        val streamMode = if (protocol == Protocol.UDP) {
            val udpPorts = when (frameType) {
                RtspFrame.FrameType.VIDEO -> ResponseManager.videoClientPorts
                RtspFrame.FrameType.AUDIO -> ResponseManager.audioClientPorts
            }
            "client_port=${udpPorts.streamPort}-${udpPorts.streamStateReportPort}"
        } else {
            "interleaved=${2 * track}-${2 * track + 1}"
        }

        return RtspMessage(
            RtspMethod.SETUP,
            "${url.rtspUrl}/streamid=$track",
            "RTSP/1.0",
            ++cSeq,
            mapOf(
                "Transport" to "RTP/AVP/${protocol.name};unicast;$streamMode;mode=record"
            ) + getDefaultHeaders()
        )
    }

    fun createRecord(): RtspMessage {
        return RtspMessage(
            RtspMethod.RECORD,
            url.rtspUrl,
            "RTSP/1.0",
            ++cSeq,
            mapOf("Range" to "npt=0.000-") + getDefaultHeaders()
        )
    }

    fun createAnnounce(): RtspMessage {
        return RtspMessage(
            method = RtspMethod.ANNOUNCE,
            url = url.rtspUrl,
            protocol = "RTSP/1.0",
            sequenceNumber = ++cSeq,
            getDefaultHeaders()
        ).attachSdpBody(createBody())
    }

    fun createAnnounceWithAuth(authHeaders: Map<String, String>, user: String, password: String): RtspMessage {
        authorization = createAuth(authHeaders, user, password)
        return createAnnounce()
    }

    fun createTeardown(): RtspMessage {
        return RtspMessage(
            RtspMethod.TEARDOWN,
            url.rtspUrl,
            "RTSP/1.0",
            ++cSeq,
            getDefaultHeaders()
        )
    }

    fun logCommand(rtspMessage: RtspMessage) {
        when (loggingLevel) {
            RtspLoggingLevel.NONE -> {}
            RtspLoggingLevel.BASIC -> {
                Log.i("RTSP", "--> ${rtspMessage.buildRequestLine()}")
            }
            RtspLoggingLevel.HEADERS -> {
                Log.i("RTSP", "--> ${rtspMessage.buildRequestLine()}")
                val headerLines = rtspMessage.buildHeaders().lineSequence()
                for (headerLine in headerLines) {
                    Log.d("RTSP", headerLine)
                }
                Log.i("RTSP", "--> END ${rtspMessage.method.name}")
            }
            RtspLoggingLevel.PLAIN_TEXT_BODY, RtspLoggingLevel.FULL -> {
                Log.i("RTSP", "--> ${rtspMessage.buildRequestLine()}")

                val headerLines = rtspMessage.buildHeaders().lineSequence()
                for (headerLine in headerLines) {
                    Log.i("RTSP", headerLine)
                }

                Log.d("RTSP", "")

                val bodyLines = rtspMessage.buildBody().lineSequence()
                for (bodyLine in bodyLines) {
                    Log.d("RTSP", bodyLine)
                }

                Log.i("RTSP", "--> END ${rtspMessage.method.name}")
            }
        }
    }

    suspend fun getResponse(readChannel: ByteReadChannel) = responseManager.getResponse(readChannel)

    companion object {
        private const val TAG = "CommandsManager"
    }
}
