package com.project.sharist.data.model

import com.project.sharist.data.model.error.AppError

sealed class GenericResult<out T> {
    data class Success<T>(val data: T) : GenericResult<T>()
    data class Error(val error: AppError) : GenericResult<Nothing>()
}