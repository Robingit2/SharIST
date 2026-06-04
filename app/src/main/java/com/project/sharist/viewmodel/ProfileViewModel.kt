package com.project.sharist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.model.review.UserCommentInput
import com.project.sharist.data.model.review.UserRatingInput
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.data.model.user.Vehicle
import com.project.sharist.data.repository.UserCommentRepository
import com.project.sharist.data.repository.UserRatingRepository
import com.project.sharist.data.repository.UserRatingStats
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.repository.VehicleRepository
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
    val vehicles: List<Vehicle> = emptyList(),
    val vehiclePhotoBytes: Map<String, ByteArray> = emptyMap(),
    val comments: List<UserComment> = emptyList(),
    val commentAuthorNames: Map<String, String> = emptyMap(),
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val ratingHistogram: Map<Int, Int> = emptyMap(),
    val commentsCount: Int = 0,
    val isOwnProfile: Boolean = false,
    val profileUserId: String? = null,
    val currentUserId: String? = null,
    val ratingDraft: Int = 5,
    val commentDraft: String = "",
    val isSavingRating: Boolean = false,
    val isSavingComment: Boolean = false,
    val isLoadingMoreComments: Boolean = false,
    val hasMoreComments: Boolean = false,
    val ratingMessage: String? = null,
    val commentMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val ratingsRepository: UserRatingRepository = UserRatingRepository(),
    private val commentsRepository: UserCommentRepository = UserCommentRepository(),
    private val vehicleRepository: VehicleRepository = VehicleRepository(),
    private val giveOrUpdateRatingUseCase: GiveOrUpdateRatingUseCase = GiveOrUpdateRatingUseCase(ratingsRepository),
    private val giveOrUpdateCommentUseCase: GiveOrUpdateCommentUseCase = GiveOrUpdateCommentUseCase(commentsRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var nextCommentsPage = 0

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
                val vehicles = vehicleRepository.getVehiclesByUser(userId)
                val vehiclePhotoBytes = loadVehiclePhotos(vehicles)
                val ratingStats = ratingsRepository.getRatingStatsByTarget(userId).getOrThrow("Unable to load ratings.")
                nextCommentsPage = 0
                val commentsPage = loadCommentsPage(userId, nextCommentsPage)
                val comments = commentsPage.comments
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
                    vehicles = vehicles,
                    vehiclePhotoBytes = vehiclePhotoBytes,
                    comments = comments,
                    commentAuthorNames = commentAuthorNames,
                    averageRating = ratingStats.averageRating,
                    ratingCount = ratingStats.ratingCount,
                    ratingHistogram = ratingStats.histogram(),
                    commentsCount = commentsPage.totalCount,
                    hasMoreComments = commentsPage.hasMore,
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
                    val ratingStats = ratingsRepository.getRatingStatsByTarget(targetId).getOrNull() ?: UserRatingStats()
                    _uiState.update {
                        it.copy(
                            isSavingRating = false,
                            averageRating = ratingStats.averageRating,
                            ratingCount = ratingStats.ratingCount,
                            ratingHistogram = ratingStats.histogram(),
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
                    nextCommentsPage = 0
                    val commentsPage = loadCommentsPage(targetId, nextCommentsPage)
                    val commentAuthorNames = loadCommentAuthorNames(commentsPage.comments)
                    _uiState.update {
                        it.copy(
                            isSavingComment = false,
                            comments = commentsPage.comments,
                            commentAuthorNames = commentAuthorNames,
                            commentsCount = commentsPage.totalCount,
                            hasMoreComments = commentsPage.hasMore,
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

    fun loadMoreComments() {
        val targetId = uiState.value.profileUserId ?: return
        val state = uiState.value

        if (state.isLoading || state.isLoadingMoreComments || !state.hasMoreComments) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreComments = true, commentMessage = null) }

            try {
                val commentsPage = loadCommentsPage(targetId, nextCommentsPage)
                val authorNames = loadCommentAuthorNames(commentsPage.comments)

                _uiState.update {
                    it.copy(
                        isLoadingMoreComments = false,
                        comments = it.comments + commentsPage.comments,
                        commentAuthorNames = it.commentAuthorNames + authorNames,
                        commentsCount = commentsPage.totalCount,
                        hasMoreComments = commentsPage.hasMore
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMoreComments = false,
                        commentMessage = exception.message ?: "Unable to load more comments."
                    )
                }
            }
        }
    }

    private suspend fun loadVehiclePhotos(vehicles: List<Vehicle>): Map<String, ByteArray> {
        return vehicles.mapNotNull { vehicle ->
            val path = vehicle.photoPath?.takeIf { it.isNotBlank() && !it.startsWith("content://") }
                ?: return@mapNotNull null
            try {
                vehicle.id to vehicleRepository.downloadVehiclePhoto(path)
            } catch (_: Exception) {
                null
            }
        }.toMap()
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

    private suspend fun loadCommentsPage(targetId: String, page: Int): CommentsPage {
        val from = page * COMMENTS_PAGE_SIZE.toLong()
        val to = from + COMMENTS_PAGE_SIZE - 1
        val result = commentsRepository.getCommentsPageByTarget(targetId, from, to)
        nextCommentsPage = page + 1

        return CommentsPage(
            comments = result.comments,
            totalCount = result.totalCount,
            hasMore = to + 1 < result.totalCount
        )
    }
}

private data class CommentsPage(
    val comments: List<UserComment>,
    val totalCount: Int,
    val hasMore: Boolean
)

private const val COMMENTS_PAGE_SIZE = 10

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
