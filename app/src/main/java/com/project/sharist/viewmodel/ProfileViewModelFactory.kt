package com.project.sharist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.UserRepository

class ProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository = userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
