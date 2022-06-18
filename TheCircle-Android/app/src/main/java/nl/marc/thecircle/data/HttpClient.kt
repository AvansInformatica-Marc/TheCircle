package nl.marc.thecircle.data

import okhttp3.OkHttpClient
import retrofit2.Converter

interface HttpClient {
    val okHttpClient: OkHttpClient

    val jsonConverter: Converter.Factory
}
