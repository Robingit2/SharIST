package com.project.sharist.ui.screen.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.usecase.RegisterUserUseCase

class SignupViewModelFactory(private val registerUserUseCase: RegisterUserUseCase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return SignupViewModel(registerUserUseCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}