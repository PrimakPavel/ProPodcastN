package com.propodcast.app.core.testing

import com.propodcast.app.core.network.ApiKeyInterceptor
import com.propodcast.app.core.network.ListenNotesApi
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Same Json setup as NetworkModule. */
val testJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

fun fixture(name: String): String =
    requireNotNull(ListenNotesTestServer::class.java.getResource("/listennotes/$name.json")) {
        "Missing fixture $name.json"
    }.readText()

/** MockWebServer + a real Retrofit [ListenNotesApi] pointing at it. */
class ListenNotesTestServer(apiKey: String = "") : AutoCloseable {
    val server = MockWebServer().apply { start() }

    val api: ListenNotesApi = Retrofit.Builder()
        .baseUrl(server.url("/api/v2/"))
        .client(OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor(apiKey)).build())
        .addConverterFactory(testJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ListenNotesApi::class.java)

    fun enqueueFixture(name: String) = enqueue(200, fixture(name))

    fun enqueue(code: Int, body: String = "{}") {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    override fun close() = server.close()
}
