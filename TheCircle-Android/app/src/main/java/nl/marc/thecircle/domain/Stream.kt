package nl.marc.thecircle.domain

import kotlinx.serialization.Serializable
import nl.marc.thecircle.utils.serialization.DateSerializer
import java.util.*

@Serializable
data class Stream(
    val streamId: String,
    val userId: String,
    val hlsPlaylistUrl: String,
    val hlsEmbedUrl: String,
    val rtspUrl: String,
    @Serializable(with = DateSerializer::class)
    val creationDate: Date,
    val userSignature: String,
    val serverSignature: String
)
