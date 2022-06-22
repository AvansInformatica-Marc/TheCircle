package nl.marc.thecircle.data

import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.marc.thecircle.R
import nl.marc.thecircle.utils.getOrNull
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class SignatureService(
    private val resources: Resources,
    private val dataStore: DataStore<Preferences>
) {
    private var userPrivateKey: PrivateKey? = null

    private var serverPublicKey: PublicKey? = null

    suspend fun loadUserPrivateKey(): PrivateKey {
        userPrivateKey?.let { return it }

        val privateKeyString = dataStore.getOrNull(PreferenceKeys.privateKey)!!

        val privateKeyBytes = privateKeyString
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("[\\s\\r\\n]*".toRegex(), "")
            .decodeBase64Bytes()
        val secretKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(secretKeySpec).also {
            userPrivateKey = it
        }
    }

    suspend fun loadServerPublicKey(): PublicKey {
        serverPublicKey?.let { return it }

        val serverKeyPem = withContext(Dispatchers.IO) {
            resources.openRawResource(R.raw.server_key).use {
                it.bufferedReader().use {
                    it.readText()
                }
            }
        }
        return loadPublicKey(serverKeyPem).also {
            serverPublicKey = it
        }
    }

    fun loadPublicKey(pem: String): PublicKey {
        val keyBytes = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("[\\s\\r\\n]*".toRegex(), "")
            .decodeBase64Bytes()
        val publicKeySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(publicKeySpec)
    }

    fun sign(text: String, privateKey: PrivateKey): ByteArray {
        val signatureUtil = Signature.getInstance("SHA512withRSA")
        signatureUtil.initSign(privateKey)
        signatureUtil.update(text.toByteArray())
        return signatureUtil.sign()
    }

    fun verify(text: String, signature: String, publicKey: PublicKey): Boolean {
        val signatureUtil = Signature.getInstance("SHA512withRSA")
        signatureUtil.initVerify(publicKey)
        signatureUtil.update(text.toByteArray())
        return signatureUtil.verify(signature.decodeBase64Bytes())
    }
}
