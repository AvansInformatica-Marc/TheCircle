package nl.marc.thecircle.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import nl.marc.thecircle.domain.RegisterStreamCommand
import nl.marc.thecircle.domain.Stream
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

        val privateKey = SignatureUtils.loadPrivateKey(dataStore)

        val signature = SignatureUtils.sign("userId:$userId;", privateKey)

        return theCircleStreamApi.registerStream(
            RegisterStreamCommand(
                userId,
                signature.encodeBase64()
            )
        )
    }

    suspend fun deleteStream(stream: Stream) {
        theCircleStreamApi.deleteStream(stream.streamId)
    }
}
