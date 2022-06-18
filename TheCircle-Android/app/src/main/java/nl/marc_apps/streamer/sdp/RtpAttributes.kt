package nl.marc_apps.streamer.sdp

object RtpAttributes {
    fun streamId(track: Int) = "control:streamid=$track"

    fun rtpMap(payload: Int, protocol: String, vararg protocolParams: Any): String {
        val protocolSpecification = if (protocolParams.isEmpty()) "" else "/" + protocolParams.joinToString(separator = "/")
        return "rtpmap:$payload $protocol$protocolSpecification"
    }

    fun fmtp(payload: Int, params: Map<String, String>): String {
        return "fmtp:$payload ${params.toList().joinToString(separator = "; ") { (key, value) -> "$key=$value" }}"
    }
}
