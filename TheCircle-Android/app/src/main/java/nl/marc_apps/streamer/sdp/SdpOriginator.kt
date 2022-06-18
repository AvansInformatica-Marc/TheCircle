package nl.marc_apps.streamer.sdp

data class SdpOriginator(
    val username: String = "-",
    val sessionId: String,
    val versionNumber: Long,
    val networkAddress: NetworkAddress
) {
    init {
        if(" " in username) {
            throw IllegalArgumentException("Username may not contain spaces")
        }
    }

    override fun toString() = buildString {
        append(username)
        append(" ")
        append(sessionId)
        append(" ")
        append(versionNumber)
        append(" ")
        append(networkAddress)
    }
}
