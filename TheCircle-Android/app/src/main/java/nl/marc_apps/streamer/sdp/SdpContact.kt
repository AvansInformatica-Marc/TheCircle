package nl.marc_apps.streamer.sdp

data class SdpContact(
    val email: String? = null,
    val phoneNumber: String? = null,
    val contactName: String? = null
) {
    init {
        if (email.isNullOrBlank() && phoneNumber.isNullOrBlank()) {
            throw IllegalArgumentException("You must provide at least an email or a phone number!")
        }
    }
}
