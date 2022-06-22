package nl.marc.thecircle.domain

import kotlinx.serialization.Serializable

@Serializable
data class AddUserCommand(
    val name: String,
    val publicKey: String,
    val userSignature: String
)
