package nl.marc.thecircle.domain

import kotlinx.serialization.Serializable
import nl.marc.thecircle.utils.serialization.DateSerializer
import java.util.*

@Serializable
data class User(
    val userId: String,
    val name: String,
    @Serializable(with = DateSerializer::class)
    val creationDate: Date,
    val publicKey: String
)
