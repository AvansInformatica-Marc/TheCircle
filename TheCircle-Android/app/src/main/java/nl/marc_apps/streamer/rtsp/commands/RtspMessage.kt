package nl.marc_apps.streamer.rtsp.commands

import nl.marc_apps.streamer.sdp.SessionDescriptionMessage
import nl.marc_apps.streamer.sdp.appendCrlf

data class RtspMessage(
    val method: RtspMethod,
    val url: String,
    val protocol: String,
    val sequenceNumber: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
) {
    fun buildRequestLine(): String {
        return buildString {
            append(method.name)
            append(" ")
            append(url)
            append(" ")
            append(protocol)
        }
    }

    fun buildHeaders(): String {
        return buildString {
            append("CSeq: ")
            append(sequenceNumber)

            for ((key, value) in headers) {
                appendCrlf()
                append(key)
                append(": ")
                append(value)
            }
        }
    }

    fun buildBody(): String {
        return buildString {
            appendCrlf()

            if (body != null) {
                append(body)
                if (!body.endsWith("\r\n")) {
                    appendCrlf()
                }
            }
        }
    }

    override fun toString(): String {
        return buildString {
            append(buildRequestLine())
            appendCrlf()

            append(buildHeaders())
            appendCrlf()

            append(buildBody())
        }
    }

    fun attachSdpBody(sessionDescriptionMessage: SessionDescriptionMessage): RtspMessage {
        val body = sessionDescriptionMessage.toString()
        val sdpHeaders = mutableMapOf(
            "Content-Type" to SessionDescriptionMessage.MIME_TYPE,
            "Content-Length" to body.length.toString()
        )

        return copy(
            headers = headers + sdpHeaders,
            body = body
        )
    }
}
