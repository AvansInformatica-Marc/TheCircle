package nl.marc.thecircle.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import nl.marc.thecircle.data.api.RegisterStreamCommand
import nl.marc.thecircle.data.api.Stream
import nl.marc.thecircle.data.api.TheCircleStreamApi
import nl.marc.thecircle.utils.getOrNull
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

class StreamRepository(
    private val dataStore: DataStore<Preferences>,
    private val theCircleStreamApi: TheCircleStreamApi
) {
    suspend fun registerStream(): Stream {
        val userId = dataStore.getOrNull(PreferenceKeys.userId)!!

        val privateKey = loadPrivateKey()

        val signature = Signature.getInstance("SHA512withRSA")
        signature.initSign(privateKey)
        signature.update("userId:${userId};".toByteArray())

        return theCircleStreamApi.registerStream(
            RegisterStreamCommand(
                userId,
                signature.sign().encodeBase64()
            )
        )
    }

    private suspend fun loadPrivateKey(): PrivateKey {
        val privateKeyString = dataStore.getOrNull(PreferenceKeys.privateKey)!!

        val privateKeyBytes = privateKeyString
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("[\\s\\r\\n]*".toRegex(), "")
            .decodeBase64Bytes()
        val secretKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(secretKeySpec)
    }

    suspend fun deleteStream(stream: Stream) {
        theCircleStreamApi.deleteStream(stream.streamId)
    }
}
