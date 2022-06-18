package nl.marc.thecircle.data.api

import kotlinx.serialization.Serializable

@Serializable
data class AddUserCommand(
    val name: String,
    val publicKey: String,
    val userSignature: String
)
