package com.project.sharist.data.repository

class AuthRepository {
    fun sendOtpToEmail(email: String) {
        // API call
    }

    fun verifyOtp(
        email: String,
        otp: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        // API verification
    }
}