package com.project.sharist.data.model.auth

sealed class RegisterResult {
    data object Success : RegisterResult()
    data class Failure(val message: String?) : RegisterResult()
}