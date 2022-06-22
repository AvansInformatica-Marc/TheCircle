package nl.marc.thecircle.data.api

import nl.marc.thecircle.domain.RegisterStreamCommand
import nl.marc.thecircle.domain.Stream
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface TheCircleStreamApi {
    @POST("v1/streams")
    suspend fun registerStream(@Body registerStreamCommand: RegisterStreamCommand): Stream

    @DELETE("v1/streams/{streamId}")
    suspend fun deleteStream(@Path("streamId") streamId: String): Response<Unit>
}
