package com.project.sharist.ui.screen.signup
import com.project.sharist.data.repository.AuthRepository
import com.project.sharist.data.usecase.auth.RegisterUserUseCase
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.auth.RegisterUserInput

class SignupViewModel(private val registerUserUseCase: RegisterUserUseCase) : ViewModel() {
    private val repository = AuthRepository()

    fun registerUser(
        state: SignupState,
        onSuccess: () -> Unit
    ) {

        val user = RegisterUserInput(
            email = "abc@gmail.com",
            password = "password",
            name = "John Doe",
        )

        viewModelScope.launch {

            registerUserUseCase(user)

            onSuccess()
        }
    }

    fun sendOtp(email: String) {
        repository.sendOtpToEmail(email)
    }

    fun verifyOtp(
        email: String,
        otp: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        repository.verifyOtp(
            email,
            otp,
            onSuccess,
            onFailure
        )
    }
}