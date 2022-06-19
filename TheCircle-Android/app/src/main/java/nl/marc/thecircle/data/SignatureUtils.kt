package nl.marc.thecircle.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import nl.marc.thecircle.utils.getOrNull
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

object SignatureUtils {
    suspend fun loadPrivateKey(dataStore: DataStore<Preferences>): PrivateKey {
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

    fun sign(text: String, privateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA512withRSA")
        signature.initSign(privateKey)
        signature.update(text.toByteArray())
        return signature.sign()
    }
}
