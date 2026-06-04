package com.project.sharist.ui.screen.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.UserRepository

class EditProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(userRepository = userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
