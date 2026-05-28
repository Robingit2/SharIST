package com.project.sharist.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.usecase.auth.LoginUserUseCase

class LoginViewModelFactory(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(loginUserUseCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}
