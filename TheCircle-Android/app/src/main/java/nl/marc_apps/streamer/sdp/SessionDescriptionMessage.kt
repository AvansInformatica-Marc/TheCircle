package nl.marc_apps.streamer.sdp

data class SessionDescriptionMessage(
    val protocolVersion: Int = 0,
    val originatorAndSessionIdentifier: SdpOriginator,
    val sessionName: String = " ",
    val sessionDescription: String? = null,
    val sessionUrl: String? = null,
    val contactInfo: SdpContact? = null,
    val connectionInfo: ConnectionInfo? = null,
    val encryptionSpecification: Pair<String, String?>? = null,
    val sessionAttributes: List<String> = emptyList(),
    val mediaDescriptions: List<SdpMediaDescription> = emptyList()
) {
    init {
        if (sessionName.isEmpty()) {
            throw IllegalArgumentException("Session name must include at least one UTF-8 character")
        }
    }

    override fun toString() = buildString {
        appendSdpField("v") {
            append(protocolVersion)
        }

        appendSdpField("o") {
            append(originatorAndSessionIdentifier)
        }

        appendSdpField("s") {
            append(sessionName)
        }

        if (!sessionDescription.isNullOrBlank()) {
            appendSdpField("i") {
                append(sessionDescription)
            }
        }

        if (!sessionUrl.isNullOrBlank()) {
            appendSdpField("u") {
                append(sessionUrl)
            }
        }

        if (contactInfo?.email != null) {
            appendSdpField("e") {
                append(contactInfo.email)

                if(contactInfo.contactName != null) {
                    append(" (")
                    append(contactInfo.contactName)
                    append(")")
                }
            }
        }

        if (contactInfo?.phoneNumber != null) {
            appendSdpField("p") {
                append(contactInfo.phoneNumber)

                if(contactInfo.contactName != null) {
                    append(" (")
                    append(contactInfo.contactName)
                    append(")")
                }
            }
        }

        if (connectionInfo != null) {
            appendSdpField("c") {
                append(connectionInfo)
            }
        }

        appendSdpField("t") {
            append("0 0")
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

        for (sessionAttribute in sessionAttributes) {
            appendSdpField("a") {
                append(sessionAttribute)
            }
        }

        for (mediaDescription in mediaDescriptions) {
            append(mediaDescription)
        }
    }

    companion object {
        const val MIME_TYPE = "application/sdp"
    }
}
