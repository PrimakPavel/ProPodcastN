package com.propodcast.app.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** Adds `X-ListenAPI-Key` when a key is configured (the mock server needs none). */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (apiKey.isBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder().header(HEADER, apiKey).build()
        }
        return chain.proceed(request)
    }

    companion object {
        const val HEADER = "X-ListenAPI-Key"
    }
}
