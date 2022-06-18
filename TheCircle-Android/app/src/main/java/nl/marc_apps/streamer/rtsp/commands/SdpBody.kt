package nl.marc_apps.streamer.rtsp.commands

import android.util.Base64
import nl.marc_apps.streamer.codecs.H265Metadata
import nl.marc_apps.streamer.codecs.H26xMetadata
import nl.marc_apps.streamer.rtsp.utils.RtspConstants
import nl.marc_apps.streamer.rtsp.utils.encodeToString
import nl.marc_apps.streamer.sdp.RtpAttributes
import nl.marc_apps.streamer.sdp.SdpMediaDescription

object SdpBody {
    private val AUDIO_SAMPLING_RATES = intArrayOf(
        96_000, // 0
        88_200, // 1
        64_000, // 2
        48_000, // 3
        44_100, // 4
        32_000, // 5
        24_000, // 6
        22_050, // 7
        16_000, // 8
        12_000, // 9
        11_025, // 10
        8_000, // 11
        7_350, // 12
        -1, // 13
        -1, // 14
        -1 // 15
    )

    enum class RtpProtocols {
        MPEG4_GENERIC, H264, H265
    }

    private fun rtpMapAttribute(payload: Int, protocol: RtpProtocols, vararg protocolParams: Any): String {
        return RtpAttributes.rtpMap(payload, protocol.name.replace("_", "-"), *protocolParams)
    }

    fun createAacBody(trackAudio: Int, sampleRate: Int, isStereo: Boolean): SdpMediaDescription {
        val sampleRateNum = AUDIO_SAMPLING_RATES.toList().indexOf(sampleRate)
        val channel = if (isStereo) 2 else 1
        val config = 2 and 0x1F shl 11 or (sampleRateNum and 0x0F shl 7) or (channel and 0x0F shl 3)
        val payload = RtspConstants.payloadType + trackAudio
        return SdpMediaDescription(
            mediaType = SdpMediaDescription.MediaType.AUDIO,
            mediaReceiverPort = 0,
            transportProtocol = "RTP/AVP",
            mediaFormats = setOf(payload),
            attributes = listOf(
                rtpMapAttribute(payload, RtpProtocols.MPEG4_GENERIC, sampleRate, channel),
                RtpAttributes.fmtp(payload, mapOf(
                    "profile-level-id" to "1",
                    "mode" to "AAC-hbr",
                    "config" to config.toString(16),
                    "sizelength" to "13",
                    "indexlength" to "3",
                    "indexdeltalength" to "3"
                )),
                RtpAttributes.streamId(trackAudio)
            )
        )
    }

    fun createVideoBody(track: Int, metadata: H26xMetadata): SdpMediaDescription {
        val spsString = metadata.sequenceParameterSet.encodeToString(Base64.NO_WRAP)

        val ppsString = metadata.pictureParameterSet.encodeToString(Base64.NO_WRAP)

        return when (metadata) {
            is H265Metadata -> createH265Body(track, spsString, ppsString, metadata.videoParameterSet.encodeToString(Base64.NO_WRAP))
            else -> createH264Body(track, spsString, ppsString)
        }
    }

    private fun createH264Body(trackVideo: Int, sps: String, pps: String): SdpMediaDescription {
        return createH26xBody(trackVideo, RtpProtocols.H264, mapOf(
            "sprop-parameter-sets" to "$sps,$pps"
        ))
    }

    private fun createH265Body(trackVideo: Int, sps: String, pps: String, vps: String): SdpMediaDescription {
        return createH26xBody(trackVideo, RtpProtocols.H265, mapOf(
            "sprop-sps" to sps,
            "sprop-pps" to pps,
            "sprop-vps" to vps
        ))
    }

    private fun createH26xBody(trackVideo: Int, protocol: RtpProtocols, parameterSetsSpecification: Map<String, String>): SdpMediaDescription {
        val payload = RtspConstants.payloadType + trackVideo
        return SdpMediaDescription(
            mediaType = SdpMediaDescription.MediaType.VIDEO,
            mediaReceiverPort = 0,
            transportProtocol = "RTP/AVP",
            mediaFormats = setOf(payload),
            attributes = listOf(
                rtpMapAttribute(payload, protocol, RtspConstants.clockVideoFrequency),
                RtpAttributes.fmtp(payload, mapOf(
                    "packetization-mode" to "1"
                ) + parameterSetsSpecification),
                RtpAttributes.streamId(trackVideo)
            )
        )
    }
}
