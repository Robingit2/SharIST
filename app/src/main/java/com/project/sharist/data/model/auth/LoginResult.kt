package com.project.sharist.data.model.auth

import com.project.sharist.data.model.user.User

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Failure(val message: String?): LoginResult()
}