package com.project.sharist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.model.review.UserCommentInput
import com.project.sharist.data.model.review.UserRatingInput
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.data.repository.UserCommentRepository
import com.project.sharist.data.repository.UserRatingRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.usecase.review.GiveOrUpdateCommentUseCase
import com.project.sharist.data.usecase.review.GiveOrUpdateRatingUseCase
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
    val avatarBytes: ByteArray? = null,
    val roles: List<RoleType> = emptyList(),
    val comments: List<UserComment> = emptyList(),
    val commentAuthorNames: Map<String, String> = emptyMap(),
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val isOwnProfile: Boolean = false,
    val profileUserId: String? = null,
    val currentUserId: String? = null,
    val ratingDraft: Int = 5,
    val commentDraft: String = "",
    val isSavingRating: Boolean = false,
    val isSavingComment: Boolean = false,
    val ratingMessage: String? = null,
    val commentMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val ratingsRepository: UserRatingRepository = UserRatingRepository(),
    private val commentsRepository: UserCommentRepository = UserCommentRepository(),
    private val giveOrUpdateRatingUseCase: GiveOrUpdateRatingUseCase = GiveOrUpdateRatingUseCase(ratingsRepository),
    private val giveOrUpdateCommentUseCase: GiveOrUpdateCommentUseCase = GiveOrUpdateCommentUseCase(commentsRepository)
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
                val avatarBytes = user.photoPath
                    ?.takeIf { it.isNotBlank() && !it.startsWith("content://") }
                    ?.let { path ->
                        try {
                            userRepository.downloadAvatar(path)
                        } catch (_: Exception) {
                            null
                        }
                    }
                val roles = userRepository.getUserRoles(userId)
                val ratings = ratingsRepository.getRatingsByTarget(userId).getOrThrow("Unable to load ratings.")
                val comments = commentsRepository.getCommentsByTarget(userId).getOrThrow("Unable to load comments.")
                val commentAuthorNames = loadCommentAuthorNames(comments)
                val ownRating = currentUserId
                    ?.takeIf { it != userId }
                    ?.let { ratingsRepository.getRatingByUsers(it, userId).getOrNull() }
                val ownComment = currentUserId
                    ?.takeIf { it != userId }
                    ?.let { commentsRepository.getCommentByUsers(it, userId).getOrNull() }

                _uiState.value = ProfileUiState(
                    user = user,
                    avatarBytes = avatarBytes,
                    roles = roles,
                    comments = comments,
                    commentAuthorNames = commentAuthorNames,
                    averageRating = ratings.map { it.rating }.averageOrZero(),
                    ratingCount = ratings.size,
                    isOwnProfile = userId == currentUserId,
                    profileUserId = userId,
                    currentUserId = currentUserId,
                    ratingDraft = ownRating?.rating ?: 5,
                    commentDraft = ownComment?.comment.orEmpty()
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

    fun updateRatingDraft(value: Int) {
        _uiState.update {
            it.copy(
                ratingDraft = value.coerceIn(1, 5),
                ratingMessage = null
            )
        }
    }

    fun updateCommentDraft(value: String) {
        _uiState.update {
            it.copy(
                commentDraft = value,
                commentMessage = null
            )
        }
    }

    fun submitRating() {
        val state = uiState.value
        val raterId = state.currentUserId
        val targetId = state.profileUserId

        if (raterId == null || targetId == null || state.isOwnProfile) {
            _uiState.update { it.copy(ratingMessage = "You can only rate another user while logged in.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingRating = true, ratingMessage = null) }

            when (val result = giveOrUpdateRatingUseCase(
                UserRatingInput(
                    raterId = raterId,
                    targetId = targetId,
                    rating = state.ratingDraft
                )
            )) {
                is GenericResult.Success -> {
                    val ratings = ratingsRepository.getRatingsByTarget(targetId).getOrNull().orEmpty()
                    _uiState.update {
                        it.copy(
                            isSavingRating = false,
                            averageRating = ratings.map { rating -> rating.rating }.averageOrZero(),
                            ratingCount = ratings.size,
                            ratingMessage = "Rating saved."
                        )
                    }
                }

                is GenericResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSavingRating = false,
                            ratingMessage = result.error.toReviewMessage("Could not save rating.")
                        )
                    }
                }
            }
        }
    }

    fun submitComment() {
        val state = uiState.value
        val raterId = state.currentUserId
        val targetId = state.profileUserId
        val comment = state.commentDraft.trim()

        if (raterId == null || targetId == null || state.isOwnProfile) {
            _uiState.update { it.copy(commentMessage = "You can only comment on another user while logged in.") }
            return
        }

        if (comment.isBlank()) {
            _uiState.update { it.copy(commentMessage = "Enter a comment before saving.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingComment = true, commentMessage = null) }

            when (val result = giveOrUpdateCommentUseCase(
                UserCommentInput(
                    raterId = raterId,
                    targetId = targetId,
                    comment = comment
                )
            )) {
                is GenericResult.Success -> {
                    val comments = commentsRepository.getCommentsByTarget(targetId).getOrNull().orEmpty()
                    val commentAuthorNames = loadCommentAuthorNames(comments)
                    _uiState.update {
                        it.copy(
                            isSavingComment = false,
                            comments = comments,
                            commentAuthorNames = commentAuthorNames,
                            commentDraft = comment,
                            commentMessage = "Comment saved."
                        )
                    }
                }

                is GenericResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSavingComment = false,
                            commentMessage = result.error.toReviewMessage("Could not save comment.")
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadCommentAuthorNames(comments: List<UserComment>): Map<String, String> {
        return comments
            .map { it.raterUserId }
            .distinct()
            .mapNotNull { userId ->
                val user = userRepository.getUser(userId).getOrNull()
                user?.let { userId to it.name }
            }
            .toMap()
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

private fun <T> GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is GenericResult.Success -> data
        is GenericResult.Error -> null
    }
}

private fun com.project.sharist.data.model.error.AppError.toReviewMessage(fallback: String): String {
    return when (this) {
        com.project.sharist.data.model.error.AppError.Network -> "Network error. Try again."
        com.project.sharist.data.model.error.AppError.Conflict -> "You already submitted this item."
        com.project.sharist.data.model.error.AppError.Unauthorized -> "You are not allowed to do that."
        com.project.sharist.data.model.error.AppError.NotFound -> "Profile or review table not found."
        is com.project.sharist.data.model.error.AppError.Unknown -> message ?: fallback
    }
}
