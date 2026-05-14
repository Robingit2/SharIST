package com.project.sharist.data.dto;

data class RegisterUserDto (
    val email: String,
    val password: String,
    val name: String,
    val photoPath: String = ""
)
