package nl.marc_apps.streamer.sdp

sealed interface NetworkAddress {
    val host: String

    data class Ipv4NetworkAddress(override val host: String): NetworkAddress {
        override fun toString() = "IN IP4 $host"
    }

    data class Ipv6NetworkAddress(override val host: String): NetworkAddress {
        override fun toString() = "IN IP6 $host"
    }
}
