package com.propodcast.app.core.network

import com.propodcast.app.core.model.ApiQuota
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/** Reads the Listen Notes usage headers from successful responses and reports them to [onQuota]. */
class ApiQuotaInterceptor(private val onQuota: (ApiQuota) -> Unit) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) {
            parseApiQuota(response.headers)?.let(onQuota)
        }
        return response
    }

    companion object {
        const val HEADER_USAGE = "X-ListenAPI-Usage"
        const val HEADER_FREE_QUOTA = "X-ListenAPI-FreeQuota"
        const val HEADER_NEXT_BILLING_DATE = "X-ListenAPI-NextBillingDate"
        const val HEADER_PLAN = "X-ListenAPI-Plan"

        /** Null when the usage or quota header is missing or not a number. */
        fun parseApiQuota(headers: Headers): ApiQuota? {
            val used = headers[HEADER_USAGE]?.trim()?.toIntOrNull() ?: return null
            val freeQuota = headers[HEADER_FREE_QUOTA]?.trim()?.toIntOrNull() ?: return null
            val nextBillingDate = headers[HEADER_NEXT_BILLING_DATE]?.let { value ->
                try {
                    OffsetDateTime.parse(value.trim()).toInstant()
                } catch (_: DateTimeParseException) {
                    null
                }
            }
            return ApiQuota(
                used = used,
                freeQuota = freeQuota,
                nextBillingDate = nextBillingDate,
                plan = headers[HEADER_PLAN]?.trim()?.takeIf { it.isNotEmpty() },
            )
        }
    }
}
