package nl.marc.thecircle.data

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
import kotlin.time.Duration.Companion.seconds

class ChatRepository(
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

        val privateKey = SignatureUtils.loadPrivateKey(dataStore)

        val signature = SignatureUtils.sign("chatId:$userId;senderId:$userId;message:$message;", privateKey)

        theCircleChatApi.sendMessage(
            userId,
            AddMessageCommand(userId, userId, message, signature.encodeBase64())
        )
    }
}
