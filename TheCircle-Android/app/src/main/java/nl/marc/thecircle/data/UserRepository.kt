package nl.marc.thecircle.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.ktor.util.*
import nl.marc.thecircle.data.api.TheCircleUserApi
import nl.marc.thecircle.domain.AddUserCommand
import nl.marc.thecircle.domain.User
import nl.marc.thecircle.utils.getOrNull
import nl.marc.thecircle.utils.serialization.JsonDateTime
import nl.marc.thecircle.utils.toPem
import java.security.KeyPairGenerator

class UserRepository(
    private val signatureService: SignatureService,
    private val dataStore: DataStore<Preferences>,
    private val theCircleUserApi: TheCircleUserApi
) {
    private val userCache = mutableMapOf<String, User>()

    suspend fun getUserById(userId: String): User? {
        return userCache[userId] ?: try {
            val user = theCircleUserApi.getUserById(userId)
            verifyUserResponse(user)
            if (userCache.size < 30) {
                userCache[userId] = user
            }
            user
        } catch (error: Throwable) {
            error.printStackTrace()
            null
        }
    }

    suspend fun register(name: String) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(4096)
        val keyPair = keyPairGenerator.generateKeyPair()

        val publicKeyString = keyPair.public.toPem()

        val signature = signatureService.sign("name:$name;publicKey:${publicKeyString.replace("\r\n", "")};", keyPair.private)

        val user = theCircleUserApi.register(
            AddUserCommand(name, publicKeyString, signature.encodeBase64())
        )

        verifyUserResponse(user)

        dataStore.edit {
            it[PreferenceKeys.privateKey] = keyPair.private.toPem()
            it[PreferenceKeys.userId] = user.userId
            it[PreferenceKeys.userName] = user.name
        }
    }

    private suspend fun verifyUserResponse(user: User): Boolean {
        val serverKey = signatureService.loadServerPublicKey()
        val verified = signatureService.verify(
            "userId:${user.userId};name:${user.name};creationDate:${JsonDateTime.defaultParser.format(user.creationDate)};" +
                "publicKey:${user.publicKey.replace("\r\n", "")};certificate:${user.certificate?.replace("\r\n", "")};" +
                "userSignature:${user.userSignature};",
            user.serverSignature,
            serverKey
        )

        if (verified) {
            Log.i("UserRepository", "Verified server response for user ${user.userId}.")
        } else {
            Log.e("UserRepository", "Invalid server response for user ${user.userId}!!!")
        }

        return verified
    }

    suspend fun isRegistered(): Boolean {
        val privateKeyString = dataStore.getOrNull(PreferenceKeys.privateKey)
        val userId = dataStore.getOrNull(PreferenceKeys.userId)
        val userName = dataStore.getOrNull(PreferenceKeys.userName)
        return privateKeyString != null && userId != null && userName != null
    }
}
