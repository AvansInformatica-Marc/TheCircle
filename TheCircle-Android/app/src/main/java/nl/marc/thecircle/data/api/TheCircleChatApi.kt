package nl.marc.thecircle.data.api

import nl.marc.thecircle.domain.AddMessageCommand
import nl.marc.thecircle.domain.Message
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TheCircleChatApi {
    @GET("v1/chat/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String): Set<Message>

    @POST("v1/chat/{chatId}/messages")
    suspend fun sendMessage(@Path("chatId") chatId: String, @Body addMessageCommand: AddMessageCommand): Message
}
