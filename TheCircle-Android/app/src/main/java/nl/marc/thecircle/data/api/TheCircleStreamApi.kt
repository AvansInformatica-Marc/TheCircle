package nl.marc.thecircle.data.api

import retrofit2.http.*

interface TheCircleStreamApi {
    @POST("v1/streams")
    suspend fun registerStream(@Body registerStreamCommand: RegisterStreamCommand): Stream

    @DELETE("v1/streams/{streamId}")
    suspend fun deleteStream(@Path("streamId") streamId: String): Unit?
}
