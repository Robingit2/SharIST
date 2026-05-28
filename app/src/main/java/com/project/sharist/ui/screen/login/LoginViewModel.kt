package com.project.sharist.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.auth.LoginUserInput
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.usecase.auth.LoginUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUserUseCase: LoginUserUseCase) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Fill email and password."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _loginSuccess.value = false

            when (val result = loginUserUseCase(LoginUserInput(email.trim(), password))) {
                is GenericResult.Success -> _loginSuccess.value = true
                is GenericResult.Error -> _errorMessage.value = result.error.toMessage("Could not login.")
            }

            _isLoading.value = false
        }
    }
}

private fun AppError.toMessage(defaultMessage: String): String {
    return when (this) {
        AppError.Network -> "Network error. Check your connection."
        AppError.Conflict -> "Conflicting account data."
        AppError.Unauthorized -> "Invalid email or password."
        AppError.NotFound -> "User profile was not found."
        is AppError.Unknown -> message ?: defaultMessage
    }
}
