package nl.marc_apps.streamer.sdp

data class ConnectionInfo(val networkAddress: NetworkAddress, val timeToLive: Int? = null) {
    override fun toString() = buildString {
        append(networkAddress)

        if (timeToLive != null && networkAddress !is NetworkAddress.Ipv6NetworkAddress) {
            append("/")
            append(timeToLive)
        }
    }
}
