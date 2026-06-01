package com.project.sharist.ui.screen.users

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.repository.UserProfileUpdate
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val photoPath: String = "",
    val saved: Boolean = false
)

class EditProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun startEditing(name: String, photoPath: String?) {
        _uiState.value = EditProfileUiState(
            name = name,
            photoPath = photoPath.orEmpty()
        )
    }

    fun loadProfile() {
        val userId = supabase.auth.currentUserOrNull()?.id

        if (userId == null) {
            _uiState.value = EditProfileUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saved = false) }

            when (val result = userRepository.getUser(userId)) {
                is GenericResult.Success -> {
                    _uiState.value = EditProfileUiState(
                        name = result.data.name,
                        photoPath = result.data.photoPath.orEmpty()
                    )
                }

                is GenericResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not load profile."
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, saved = false) }
    }

    fun onPhotoPathChange(photoPath: String) {
        _uiState.update { it.copy(photoPath = photoPath, saved = false) }
    }

    fun saveProfile(context: Context) {
        val userId = supabase.auth.currentUserOrNull()?.id
        val state = _uiState.value

        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "No logged in user found.") }
            return
        }

        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saved = false) }

            try {
                val photoPath = state.photoPath.trim()
                val savedPhotoPath = if (photoPath.startsWith("content://")) {
                    userRepository.uploadAvatar(context, userId, Uri.parse(photoPath))
                } else {
                    photoPath.takeIf { it.isNotEmpty() }
                }

                userRepository.updateProfile(
                    userId = userId,
                    update = UserProfileUpdate(
                        name = state.name.trim(),
                        photoPath = savedPhotoPath
                    )
                )
                _uiState.update { it.copy(isLoading = false, saved = true) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not save profile."
                    )
                }
            }
        }
    }
}
