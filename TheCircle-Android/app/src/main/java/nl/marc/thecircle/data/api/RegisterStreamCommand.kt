package nl.marc.thecircle.data.api

import kotlinx.serialization.Serializable

@Serializable
data class RegisterStreamCommand(
    val userId: String,
    val userSignature: String
)
