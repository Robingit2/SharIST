package com.project.sharist.data.model

import com.project.sharist.data.model.error.AppError

sealed class GenericResult<out T> {
    data class Success<T>(val data: T) : GenericResult<T>()
    data class Error(val error: AppError) : GenericResult<Nothing>()
}

fun <T> GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is GenericResult.Success -> data
        is GenericResult.Error -> null
    }
}

fun <T> GenericResult<T>.getOrThrow(message: String): T {
    return when (this) {
        is GenericResult.Success -> data
        is GenericResult.Error -> throw IllegalStateException(message)
    }
}

fun AppError.toMessage(fallback: String): String {
    return when (this) {
        AppError.Network -> "Network error."
        AppError.Conflict -> "This action conflicts with existing data."
        AppError.Unauthorized -> "You are not allowed to do this."
        AppError.NotFound -> "The requested data was not found."
        is AppError.Unknown -> message ?: fallback
    }
}
