package nl.marc.thecircle.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.ktor.util.*
import nl.marc.thecircle.data.api.AddUserCommand
import nl.marc.thecircle.data.api.TheCircleUserApi
import nl.marc.thecircle.utils.getOrNull
import nl.marc.thecircle.utils.toPem
import java.security.KeyPairGenerator
import java.security.Signature

class UserRepository (
    private val dataStore: DataStore<Preferences>,
    private val theCircleUserApi: TheCircleUserApi
) {
    suspend fun register(name: String) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(4096)
        val keyPair = keyPairGenerator.generateKeyPair()

        val publicKeyString = keyPair.public.toPem()

        val signature = Signature.getInstance("SHA512withRSA")
        signature.initSign(keyPair.private)
        signature.update("name:${name};publicKey:${publicKeyString};".toByteArray())

        val user = theCircleUserApi.register(
            AddUserCommand(name, publicKeyString, signature.sign().encodeBase64())
        )

        dataStore.edit {
            it[PreferenceKeys.privateKey] = keyPair.private.toPem()
            it[PreferenceKeys.userId] = user.userId
            it[PreferenceKeys.userName] = user.name
        }
    }

    suspend fun isRegistered(): Boolean {
        val privateKeyString = dataStore.getOrNull(PreferenceKeys.privateKey)
        val userId = dataStore.getOrNull(PreferenceKeys.userId)
        val userName = dataStore.getOrNull(PreferenceKeys.userName)
        return privateKeyString != null && userId != null && userName != null
    }
}
