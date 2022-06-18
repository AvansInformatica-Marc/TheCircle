package nl.marc.thecircle.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import nl.marc.thecircle.data.api.RegisterStreamCommand
import nl.marc.thecircle.data.api.Stream
import nl.marc.thecircle.data.api.TheCircleStreamApi
import nl.marc.thecircle.utils.getOrNull
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

class StreamRepository(
    private val dataStore: DataStore<Preferences>,
    private val theCircleStreamApi: TheCircleStreamApi
) {
    suspend fun registerStream(): Stream {
        val privateKeyString = dataStore.getOrNull(PreferenceKeys.privateKey)!!
        val userId = dataStore.getOrNull(PreferenceKeys.userId)!!

        val secretKeySpec = PKCS8EncodedKeySpec(privateKeyString.decodeBase64Bytes())
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(secretKeySpec)

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

    suspend fun deleteStream(stream: Stream) {
        theCircleStreamApi.deleteStream(stream.streamId)
    }
}
