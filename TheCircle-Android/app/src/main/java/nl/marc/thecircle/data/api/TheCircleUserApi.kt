package nl.marc.thecircle.data.api

import nl.marc.thecircle.domain.AddUserCommand
import nl.marc.thecircle.domain.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TheCircleUserApi {
    @GET("v1/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): User

    @POST("v1/users")
    suspend fun register(@Body addUserCommand: AddUserCommand): User
}
