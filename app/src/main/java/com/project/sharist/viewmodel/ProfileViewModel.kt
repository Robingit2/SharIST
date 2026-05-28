package com.project.sharist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.data.repository.UserCommentRepository
import com.project.sharist.data.repository.UserRatingRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val roles: List<RoleType> = emptyList(),
    val comments: List<UserComment> = emptyList(),
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val isOwnProfile: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val ratingsRepository: UserRatingRepository = UserRatingRepository(),
    private val commentsRepository: UserCommentRepository = UserCommentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadCurrentUserProfile() {
        val currentUserId = supabase.auth.currentUserOrNull()?.id

        if (currentUserId == null) {
            _uiState.value = ProfileUiState(errorMessage = "No logged in user found.")
            return
        }

        loadProfile(userId = currentUserId, currentUserId = currentUserId)
    }

    fun loadProfile(userId: String, currentUserId: String?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isOwnProfile = userId == currentUserId
                )
            }

            try {
                val user = userRepository.getUser(userId).getOrThrow("Unable to load user.")
                val roles = userRepository.getUserRoles(userId)
                val ratings = ratingsRepository.getRatingsByTarget(userId).getOrThrow("Unable to load ratings.")
                val comments = commentsRepository.getCommentsByTarget(userId).getOrThrow("Unable to load comments.")

                _uiState.value = ProfileUiState(
                    user = user,
                    roles = roles,
                    comments = comments,
                    averageRating = ratings.map { it.rating }.averageOrZero(),
                    ratingCount = ratings.size,
                    isOwnProfile = userId == currentUserId
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Unable to load profile."
                    )
                }
            }
        }
    }
}

private fun List<Int>.averageOrZero(): Double {
    return if (isEmpty()) 0.0 else average()
}

private fun <T> GenericResult<T>.getOrThrow(message: String): T {
    return when (this) {
        is GenericResult.Success -> data
        is GenericResult.Error -> throw IllegalStateException(message)
    }
}
