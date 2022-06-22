package nl.marc.thecircle.domain

import kotlinx.serialization.Serializable

@Serializable
data class AddMessageCommand(
    val chatId: String,
    val senderId: String,
    val message: String,
    val senderSignature: String
)
