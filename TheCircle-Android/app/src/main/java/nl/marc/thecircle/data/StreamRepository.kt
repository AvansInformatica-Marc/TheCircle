package nl.marc.thecircle.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import nl.marc.thecircle.data.api.TheCircleStreamApi
import nl.marc.thecircle.domain.RegisterStreamCommand
import nl.marc.thecircle.domain.Stream
import nl.marc.thecircle.utils.getOrNull
import nl.marc.thecircle.utils.serialization.JsonDateTime

class StreamRepository(
    private val signatureService: SignatureService,
    private val dataStore: DataStore<Preferences>,
    private val theCircleStreamApi: TheCircleStreamApi
) {
    suspend fun registerStream(): Stream {
        val userId = dataStore.getOrNull(PreferenceKeys.userId)!!

        val privateKey = signatureService.loadUserPrivateKey()

        val signature = signatureService.sign("userId:$userId;", privateKey)

        val stream = theCircleStreamApi.registerStream(
            RegisterStreamCommand(
                userId,
                signature.encodeBase64()
            )
        )

        verifyStreamResponse(stream)

        return stream
    }

    private suspend fun verifyStreamResponse(stream: Stream): Boolean {
        val serverKey = signatureService.loadServerPublicKey()
        val verified = signatureService.verify(
            "userId:${stream.userId};streamId:${stream.streamId};creationDate:${JsonDateTime.defaultParser.format(stream.creationDate)};" +
                "rtspUrl:${stream.rtspUrl};hlsEmbedUrl:${stream.hlsEmbedUrl};hlsPlaylistUrl:${stream.hlsPlaylistUrl};" +
                "userSignature:${stream.userSignature};",
            stream.serverSignature,
            serverKey
        )

        if (verified) {
            Log.i("StreamRepository", "Verified server response for stream ${stream.streamId}.")
        } else {
            Log.e("StreamRepository", "Invalid server response for stream ${stream.streamId}!!!")
        }

        return verified
    }

    suspend fun deleteStream(stream: Stream) {
        theCircleStreamApi.deleteStream(stream.streamId)
    }
}
