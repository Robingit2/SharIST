package com.project.sharist.ui.screen.signup

data class SignupState(
    val step: Int = 1,
    val userRole: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val isEmailVerified: Boolean = false,
    val verificationToken: String? = null,
    val otp:String? = "",

    val password: String = "",
    val confirmpassword: String = "",
    val identity_doc: String = "",
    val vehicleNumber: String = "",
    val vehicleModel: String = ""
)