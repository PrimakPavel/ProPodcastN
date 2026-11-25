package com.propodcast.app.core.common

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/** Errors the UI knows how to explain to the user. */
sealed interface AppError {
    /** 401: missing or wrong Listen Notes API key. */
    data object Unauthorized : AppError

    /** 404: the podcast / episode no longer exists. */
    data object NotFound : AppError

    /** 429: free plan quota exceeded. */
    data object QuotaExceeded : AppError

    /** 5xx or any other unexpected HTTP status. */
    data class Server(val code: Int) : AppError

    /** No connection, timeout, DNS, etc. */
    data object Network : AppError

    /** Response could not be parsed, or anything else. */
    data class Unknown(val cause: Throwable?) : AppError
}

/** Carries an [AppError] through Result / Paging error channels. */
class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.toString(), cause)

fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is HttpException -> when (code()) {
        401 -> AppError.Unauthorized
        404 -> AppError.NotFound
        429 -> AppError.QuotaExceeded
        else -> AppError.Server(code())
    }
    is IOException -> AppError.Network
    is SerializationException -> AppError.Unknown(this)
    else -> AppError.Unknown(this)
}

/** Runs a network call; any failure is converted to [AppException]. Cancellation is rethrown. */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(AppException(e.toAppError(), e))
}
