package com.project.sharist.data.model

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Failure(val message: String): LoginResult()
}