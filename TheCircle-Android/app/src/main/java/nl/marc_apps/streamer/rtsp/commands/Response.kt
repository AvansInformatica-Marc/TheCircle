package nl.marc_apps.streamer.rtsp.commands

data class Response(
    val protocol: String,
    val statusCode: Int,
    val statusMessage: String? = null,
    val headers: Map<String, String>,
    val body: String? = null
) {
    val sessionId by lazy {
        headers["session"]?.substringBefore(";")?.trim()
    }

    val serverPorts by lazy {
        val transportHeader = headers["transport"]?.split(";")
        val serverPortProperty = transportHeader?.firstOrNull { it.contains("server_port", ignoreCase = true) }
        val ports = serverPortProperty
            ?.substringAfter("=")
            ?.split("-")
        val first = ports?.getOrNull(0)?.trim()?.toIntOrNull()
        val second = ports?.getOrNull(1)?.trim()?.toIntOrNull()
        if (first == null && second == null) null
        else first to second
    }

    private enum class ParsingContext { RESPONSE_LINE, HEADERS, BODY }

    companion object {
        fun parse(text: String): Response {
            val lines = text.lineSequence()

            val responseLine = text.lineSequence().first()
            val responseLineSplit = responseLine.split(" ")

            val headers = mutableMapOf<String, String>()

            var body: String? = null

            var parsingContext = ParsingContext.RESPONSE_LINE

            for (line in lines) {
                when (parsingContext) {
                    ParsingContext.RESPONSE_LINE -> {
                        parsingContext = ParsingContext.HEADERS
                    }
                    ParsingContext.HEADERS -> {
                        if (line.isBlank()) {
                            parsingContext = ParsingContext.BODY
                        } else {
                            val (key, value) = line.split(":")
                            headers += key.trim().lowercase() to value.trim()
                        }
                    }
                    ParsingContext.BODY -> {
                        body = (body ?: "") + line + "\r\n"
                    }
                }
            }

            return Response(
                responseLineSplit[0],
                responseLineSplit[1].toInt(),
                responseLineSplit.getOrNull(2),
                headers,
                body
            )
        }
    }
}
