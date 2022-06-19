package nl.marc.thecircle.domain

import kotlinx.serialization.Serializable
import nl.marc.thecircle.utils.serialization.DateSerializer
import java.util.*

@Serializable
data class Message(
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val message: String,
    @Serializable(with = DateSerializer::class)
    val creationDate: Date,
    val senderSignature: String
)
