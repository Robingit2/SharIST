package com.project.sharist.data.usecase

data class RegisterUserInput (
    val email: String,
    val password: String,
    val name: String,
    val photoPath: String = ""
)
