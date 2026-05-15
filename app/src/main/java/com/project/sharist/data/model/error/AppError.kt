package com.project.sharist.data.model.error

sealed class AppError {
    data object Network : AppError()
    data object Conflict : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data object Unknown : AppError()
}