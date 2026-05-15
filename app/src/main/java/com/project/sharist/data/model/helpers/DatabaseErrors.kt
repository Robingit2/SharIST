package com.project.sharist.data.model.helpers

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.error.AppError
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

fun Exception.toAppError(): AppError {
    return when (this) {

        // Network issues (no internet, timeout, DNS, etc.)
        is IOException -> AppError.Network

        // Ktor HTTP errors (used by Supabase Kotlin client under the hood)
        is ResponseException -> when (this.response.status.value) {
            401 -> AppError.Unauthorized
            403 -> AppError.Unauthorized
            404 -> AppError.NotFound
            409 -> AppError.Conflict
            else -> AppError.Unknown
        }

        else -> AppError.Unknown
    }
}

suspend inline fun <T> safeSupabaseCall(
    crossinline block: suspend () -> T
): GenericResult<T> {
    return try {
        val result = block()
        GenericResult.Success(result)
    } catch (e: Exception) {
        GenericResult.Error(e.toAppError())
    }
}
