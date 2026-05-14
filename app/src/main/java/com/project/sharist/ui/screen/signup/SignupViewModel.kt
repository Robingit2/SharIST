package com.project.sharist.ui.screen.signup
import com.project.sharist.data.repository.AuthRepository
import com.project.sharist.domain.usecase.RegisterUserUseCase
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.project.sharist.domain.model.User

class SignupViewModel(private val registerUserUseCase: RegisterUserUseCase) : ViewModel() {
    private val repository = AuthRepository()

    fun registerUser(
        state: SignupState,
        onSuccess: () -> Unit
    ) {

        val user = User(
            userRole = state.userRole,
            vehicleNumber = state.vehicleNumber,
            vehicleModel = state.vehicleModel,
            email = state.email,
            address = state.address,
            fullName = state.fullName,
            phone = state.phone,
            document = state.identity_doc,
            password = state.password
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