package com.project.sharist.ui.screen.signup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.auth.RegisterUserInput
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.usecase.auth.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel(private val registerUserUseCase: RegisterUserUseCase) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun registerUser(
        context: Context,
        state: SignupState,
        onSuccess: () -> Unit
    ) {
        if (!state.isValid()) {
            _errorMessage.value = "Fill name, email, password, and select at least one role."
            return
        }

        val user = RegisterUserInput(
            email = state.email.trim(),
            password = state.password,
            name = state.name.trim(),
            photoPath = state.photoPath.trim(),
            roles = state.roles
        )

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = registerUserUseCase(context, user)) {
                is GenericResult.Success -> onSuccess()
                is GenericResult.Error -> _errorMessage.value = result.error.toMessage("Could not create account.")
            }

            _isLoading.value = false
        }
    }
}

private fun SignupState.isValid(): Boolean {
    return name.isNotBlank() &&
        email.isNotBlank() &&
        password.isNotBlank() &&
        roles.isNotEmpty()
}

private fun AppError.toMessage(defaultMessage: String): String {
    return when (this) {
        AppError.Network -> "Network error. Check your connection."
        AppError.Conflict -> "An account with this data already exists."
        AppError.Unauthorized -> "Could not authenticate with Supabase."
        AppError.NotFound -> "Required backend data was not found."
        is AppError.Unknown -> message ?: defaultMessage
    }
}
