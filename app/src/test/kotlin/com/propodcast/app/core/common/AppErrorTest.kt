package com.propodcast.app.core.common

import com.propodcast.app.core.testing.ListenNotesTestServer
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppErrorTest {

    @Test
    fun httpCodes_mapToAppErrors() = runTest {
        val expected = mapOf(
            401 to AppError.Unauthorized,
            404 to AppError.NotFound,
            429 to AppError.QuotaExceeded,
            503 to AppError.Server(503),
        )
        ListenNotesTestServer().use { server ->
            expected.forEach { (code, error) ->
                server.enqueue(code)
                val result = apiCall { server.api.languages() }
                assertEquals("HTTP $code", error, (result.exceptionOrNull() as AppException).error)
            }
        }
    }

    @Test
    fun ioException_isNetworkError() = runTest {
        val result = apiCall<Unit> { throw IOException("offline") }
        assertEquals(AppError.Network, (result.exceptionOrNull() as AppException).error)
    }

    @Test
    fun malformedBody_isUnknownError() = runTest {
        ListenNotesTestServer().use { server ->
            server.enqueue(200, "not json")
            val result = apiCall { server.api.languages() }
            assertTrue((result.exceptionOrNull() as AppException).error is AppError.Unknown)
        }
    }
}
