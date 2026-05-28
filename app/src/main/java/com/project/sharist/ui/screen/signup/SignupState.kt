package com.project.sharist.ui.screen.signup

data class SignupState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val photoPath: String = "",
    val roles: List<String> = emptyList()
)
