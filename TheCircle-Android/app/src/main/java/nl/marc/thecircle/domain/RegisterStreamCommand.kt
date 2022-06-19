package nl.marc.thecircle.domain

import kotlinx.serialization.Serializable

@Serializable
data class RegisterStreamCommand(
    val userId: String,
    val userSignature: String
)
