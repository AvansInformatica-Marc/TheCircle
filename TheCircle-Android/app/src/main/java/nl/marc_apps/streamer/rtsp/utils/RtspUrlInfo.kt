package nl.marc_apps.streamer.rtsp.utils

import java.util.regex.Pattern

data class RtspUrlInfo internal constructor(
    val fullUrl: String,
    val host: String,
    val port: Int,
    val streamName: String,
    val path: String,
    val isTlsEnabled: Boolean
) {
    val protocol: String
        get() = if(isTlsEnabled) RTSP_SECURE_PROTOCOL_NAME else RTSP_PROTOCOL_NAME

    val rtspUrl: String
        get() = "$protocol://$host:$port$path"

    companion object {
        private val rtspUrlPattern = Pattern.compile("^rtsps?://([^/:]+)(?::(\\d+))*/([^/]+)/?([^*]*)$")

        private const val RTSP_PROTOCOL_NAME = "rtsp"

        private const val RTSP_SECURE_PROTOCOL_NAME = "rtsps"

        private const val RTSP_PORT_SECURE = 443

        private const val RTSP_PORT_UNSECURED = 554

        @Throws(IllegalArgumentException::class)
        fun parseUrl(url: String): RtspUrlInfo {
            val rtspMatcher = rtspUrlPattern.matcher(url)
            if (!rtspMatcher.matches()) {
                throw IllegalArgumentException("Not an RTSP URL (rtsp://ip:port/appname/streamname): $url")
            }

            val tlsEnabled = (rtspMatcher.group(0) ?: "").startsWith(RTSP_SECURE_PROTOCOL_NAME)
            val host = rtspMatcher.group(1) ?: ""
            val port = rtspMatcher.group(2)?.toInt() ?: if (tlsEnabled) RTSP_PORT_SECURE else RTSP_PORT_UNSECURED
            val streamName = if (rtspMatcher.group(4).isNullOrEmpty()) "" else "/" + rtspMatcher.group(4)
            val path = "/" + rtspMatcher.group(3) + streamName

            return RtspUrlInfo(url, host, port, streamName, path, tlsEnabled)
        }
    }
}
