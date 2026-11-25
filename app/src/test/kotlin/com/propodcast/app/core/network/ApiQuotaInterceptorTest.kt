package com.propodcast.app.core.network

import com.propodcast.app.core.model.ApiQuota
import java.time.Instant
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiQuotaInterceptorTest {

    private val server = MockWebServer().apply { start() }
    private val received = mutableListOf<ApiQuota>()
    private val client = OkHttpClient.Builder()
        .addInterceptor(ApiQuotaInterceptor { received += it })
        .build()

    @After
    fun tearDown() = server.close()

    private fun call(code: Int, vararg headers: Pair<String, String>) {
        server.enqueue(
            MockResponse.Builder()
                .code(code)
                .apply { headers.forEach { (name, value) -> addHeader(name, value) } }
                .body("{}")
                .build(),
        )
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
    }

    @Test
    fun successfulResponse_reportsQuota() {
        // Values as sent by listen-api-test.listennotes.com.
        call(
            200,
            "X-ListenAPI-Usage" to "1024",
            "X-ListenAPI-FreeQuota" to "2500",
            "X-ListenAPI-NextBillingDate" to "2026-12-11T18:53:16.385542+00:00",
            "X-ListenAPI-Plan" to "FREE",
        )

        assertEquals(
            ApiQuota(
                used = 1024,
                freeQuota = 2500,
                nextBillingDate = Instant.parse("2026-12-11T18:53:16.385542Z"),
                plan = "FREE",
            ),
            received.single(),
        )
    }

    @Test
    fun errorResponse_isIgnored() {
        call(429, "X-ListenAPI-Usage" to "2500", "X-ListenAPI-FreeQuota" to "2500")

        assertEquals(emptyList<ApiQuota>(), received)
    }

    @Test
    fun missingHeaders_isIgnored() {
        call(200)
        call(200, "X-ListenAPI-Usage" to "10")

        assertEquals(emptyList<ApiQuota>(), received)
    }

    @Test
    fun parse_badDateAndMissingPlan_keepNumbers() {
        val quota = ApiQuotaInterceptor.parseApiQuota(
            headersOf(
                "X-ListenAPI-Usage", " 5 ",
                "X-ListenAPI-FreeQuota", "300",
                "X-ListenAPI-NextBillingDate", "not a date",
            ),
        )!!

        assertEquals(5, quota.used)
        assertEquals(300, quota.freeQuota)
        assertNull(quota.nextBillingDate)
        assertNull(quota.plan)
    }

    @Test
    fun parse_nonNumericUsage_isNull() {
        assertNull(
            ApiQuotaInterceptor.parseApiQuota(
                headersOf("X-ListenAPI-Usage", "n/a", "X-ListenAPI-FreeQuota", "300"),
            ),
        )
    }
}
