package nl.marc.thecircle.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import nl.marc.thecircle.data.api.TheCircleChatApi
import nl.marc.thecircle.domain.AddMessageCommand
import nl.marc.thecircle.domain.Message
import nl.marc.thecircle.utils.getOrNull
import nl.marc.thecircle.utils.serialization.DateSerializer
import nl.marc.thecircle.utils.serialization.JsonDateTime
import kotlin.time.Duration.Companion.seconds

class ChatRepository(
    private val signatureService: SignatureService,
    private val dataStore: DataStore<Preferences>,
    private val theCircleChatApi: TheCircleChatApi
) {
    val messageFlow: Flow<Set<Message>> = flow {
        while (true) {
            emit(getMessages())
            delay(3.seconds)
        }
    }

    suspend fun getMessages(): Set<Message> {
        val chatId = dataStore.getOrNull(PreferenceKeys.userId)!!
        return theCircleChatApi.getMessages(chatId)
    }

    suspend fun getCurrentUserId(): String {
        return dataStore.getOrNull(PreferenceKeys.userId)!!
    }

    suspend fun sendMessage(message: String) {
        val userId = dataStore.getOrNull(PreferenceKeys.userId)!!

        val privateKey = signatureService.loadUserPrivateKey()

        val signature = signatureService.sign("chatId:$userId;senderId:$userId;message:$message;", privateKey)

        val sentMessage = theCircleChatApi.sendMessage(
            userId,
            AddMessageCommand(userId, userId, message, signature.encodeBase64())
        )

        verifyMessageResponse(sentMessage)
    }

    private suspend fun verifyMessageResponse(message: Message): Boolean {
        val serverKey = signatureService.loadServerPublicKey()
        val verified = signatureService.verify(
            "messageId:${message.messageId};chatId:${message.chatId};senderId:${message.senderId};" +
                "creationDate:${JsonDateTime.defaultParser.format(message.creationDate)};message:${message.message};senderSignature:${message.senderSignature};",
            message.serverSignature,
            serverKey
        )

        if (verified) {
            Log.i("ChatRepository", "Verified server response for message ${message.messageId}.")
        } else {
            Log.e("ChatRepository", "Invalid server response for message ${message.messageId}!!!")
        }

        return verified
    }
}
