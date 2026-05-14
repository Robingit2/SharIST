package com.project.sharist.ui.screen.login


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    fun login(email: String, password: String) {

        _isLoading.value = true

        if (email.isNotEmpty() && password.isNotEmpty()) {
            _loginSuccess.value = true
        }

        _isLoading.value = false
    }
}