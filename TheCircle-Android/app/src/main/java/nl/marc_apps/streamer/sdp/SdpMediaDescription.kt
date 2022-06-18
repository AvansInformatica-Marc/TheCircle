package nl.marc_apps.streamer.sdp

data class SdpMediaDescription(
    val mediaType: MediaType,
    val mediaReceiverPort: Int,
    val transportProtocol: String,
    val mediaFormats: Set<Int>,
    val mediaTitle: String? = null,
    val connectionInfo: ConnectionInfo? = null,
    val encryptionSpecification: Pair<String, String?>? = null,
    val attributes: List<String> = emptyList()
) {
    enum class MediaType {
        AUDIO, VIDEO, TEXT, APPLICATION, MESSAGE
    }

    override fun toString() = buildString {
        appendSdpField("m") {
            append(mediaType.name.lowercase())
            append(" ")
            append(mediaReceiverPort)
            append(" ")
            append(transportProtocol)
            append(" ")
            mediaFormats.joinTo(this, separator = " ")
        }

        if (!mediaTitle.isNullOrBlank()) {
            appendSdpField("i") {
                append(mediaTitle)
            }
        }

        if (connectionInfo != null) {
            appendSdpField("c") {
                append(connectionInfo)
            }
        }

        if (encryptionSpecification != null) {
            val (method, key) = encryptionSpecification

            appendSdpField("k") {
                append(method)

                if (key != null) {
                    append(":")
                    append(key)
                }
            }
        }

        for (attribute in attributes) {
            appendSdpField("a") {
                append(attribute)
            }
        }
    }
}
