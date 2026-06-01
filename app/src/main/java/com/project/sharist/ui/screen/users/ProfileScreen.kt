package com.project.sharist.ui.screen.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.viewmodel.ProfileUiState
import com.project.sharist.viewmodel.ProfileViewModel
import java.util.Locale

@Composable
fun ProfileScreen(
    profileUserId: String? = null,
    currentUserId: String? = null,
    viewModel: ProfileViewModel = viewModel(),
    editProfileViewModel: EditProfileViewModel = viewModel(),
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editUiState by editProfileViewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profileUserId, currentUserId) {
        if (profileUserId == null) {
            viewModel.loadCurrentUserProfile()
        } else {
            viewModel.loadProfile(
                userId = profileUserId,
                currentUserId = currentUserId
            )
        }
    }

    LaunchedEffect(editUiState.saved) {
        if (editUiState.saved) {
            showEditDialog = false
            if (profileUserId == null) {
                viewModel.loadCurrentUserProfile()
            } else {
                viewModel.loadProfile(
                    userId = profileUserId,
                    currentUserId = currentUserId
                )
            }
        }
    }

    ProfileContent(
        uiState = uiState,
        onEditProfileClick = {
            uiState.user?.let { user ->
                editProfileViewModel.startEditing(
                    name = user.name,
                    photoPath = user.photoPath
                )
                showEditDialog = true
            }
        },
        onSettingsClick = onSettingsClick,
        onLogoutClick = onLogoutClick,
        onRatingChange = viewModel::updateRatingDraft,
        onSaveRating = viewModel::submitRating,
        onCommentChange = viewModel::updateCommentDraft,
        onSaveComment = viewModel::submitComment
    )

    if (showEditDialog) {
        EditProfileDialog(
            uiState = editUiState,
            onNameChange = editProfileViewModel::onNameChange,
            onPhotoPathChange = editProfileViewModel::onPhotoPathChange,
            onSave = editProfileViewModel::saveProfile,
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onSaveRating: () -> Unit,
    onCommentChange: (String) -> Unit,
    onSaveComment: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            uiState.errorMessage != null -> ErrorCard(
                message = uiState.errorMessage,
                modifier = Modifier.align(Alignment.Center)
            )

            uiState.user == null -> Text(
                text = "Profile not found.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )

            else -> ProfileLoadedContent(
                uiState = uiState,
                onEditProfileClick = onEditProfileClick,
                onSettingsClick = onSettingsClick,
                onLogoutClick = onLogoutClick,
                onRatingChange = onRatingChange,
                onSaveRating = onSaveRating,
                onCommentChange = onCommentChange,
                onSaveComment = onSaveComment
            )
        }
    }
}

@Composable
private fun ProfileLoadedContent(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onSaveRating: () -> Unit,
    onCommentChange: (String) -> Unit,
    onSaveComment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(user = uiState.user!!)

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isOwnProfile) {
            ProfileActions(
                onEditProfileClick = onEditProfileClick,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        ProfileStats(
            averageRating = uiState.averageRating,
            ratingCount = uiState.ratingCount,
            commentsCount = uiState.comments.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileDetails(
            user = uiState.user,
            roles = uiState.roles,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!uiState.isOwnProfile) {
            ReviewActionsSection(
                rating = uiState.ratingDraft,
                comment = uiState.commentDraft,
                isSavingRating = uiState.isSavingRating,
                isSavingComment = uiState.isSavingComment,
                ratingMessage = uiState.ratingMessage,
                commentMessage = uiState.commentMessage,
                onRatingChange = onRatingChange,
                onSaveRating = onSaveRating,
                onCommentChange = onCommentChange,
                onSaveComment = onSaveComment
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        CommentsSection(
            comments = uiState.comments,
            authorNames = uiState.commentAuthorNames
        )

        if (uiState.isOwnProfile) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile picture",
            modifier = Modifier.size(112.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileActions(
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onEditProfileClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Edit profile",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedButton(
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Settings",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    uiState: EditProfileUiState,
    onNameChange: (String) -> Unit,
    onPhotoPathChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.photoPath,
                    onValueChange = onPhotoPathChange,
                    label = { Text("Photo path") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !uiState.isLoading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProfileStats(
    averageRating: Double,
    ratingCount: Int,
    commentsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Rating",
            value = String.format(Locale.US, "%.1f", averageRating),
            supportingText = "$ratingCount ratings",
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )

        StatCard(
            label = "Comments",
            value = commentsCount.toString(),
            supportingText = "comments",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileDetails(
    user: User,
    roles: List<RoleType>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            DetailRow(label = "Name", value = user.name)

            if (roles.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { role ->
                        AssistChip(
                            onClick = {},
                            label = { Text(role.name.lowercase().replaceFirstChar { it.titlecase() }) }
                        )
                    }
                }
            } else {
                DetailRow(label = "Roles", value = "No roles found")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CommentsSection(
    comments: List<UserComment>,
    authorNames: Map<String, String>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (comments.isEmpty()) {
                Text(
                    text = "No comments yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        authorName = authorNames[comment.raterUserId] ?: "Unknown user"
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: UserComment,
    authorName: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = comment.comment,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "By $authorName" + (comment.createdAt?.toShortDate()?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewActionsSection(
    rating: Int,
    comment: String,
    isSavingRating: Boolean,
    isSavingComment: Boolean,
    ratingMessage: String?,
    commentMessage: String?,
    onRatingChange: (Int) -> Unit,
    onSaveRating: () -> Unit,
    onCommentChange: (String) -> Unit,
    onSaveComment: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RatingInputCard(
            rating = rating,
            isSaving = isSavingRating,
            message = ratingMessage,
            onRatingChange = onRatingChange,
            onSave = onSaveRating
        )

        CommentInputCard(
            comment = comment,
            isSaving = isSavingComment,
            message = commentMessage,
            onCommentChange = onCommentChange,
            onSave = onSaveComment
        )
    }
}

@Composable
private fun RatingInputCard(
    rating: Int,
    isSaving: Boolean,
    message: String?,
    onRatingChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add rating",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { value ->
                    IconButton(
                        onClick = { onRatingChange(value) },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "$value star rating",
                            modifier = Modifier.size(32.dp),
                            tint = if (value <= rating) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Saving..." else "Save rating")
                }
            }

            ReviewMessage(message = message)
        }
    }
}

@Composable
private fun CommentInputCard(
    comment: String,
    isSaving: Boolean,
    message: String?,
    onCommentChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add comment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comment") },
                minLines = 3,
                enabled = !isSaving
            )

            Button(
                onClick = onSave,
                enabled = !isSaving && comment.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving..." else "Save comment")
            }

            ReviewMessage(message = message)
        }
    }
}

@Composable
private fun ReviewMessage(message: String?) {
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun String.toShortDate(): String? {
    val date = substringBefore("T")
    val parts = date.split("-")
    if (parts.size != 3) return null

    val year = parts[0]
    val month = parts[1].toIntOrNull()?.toString() ?: return null
    val day = parts[2].toIntOrNull()?.toString() ?: return null

    return "$day/$month/$year"
}

@Composable
private fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Could not load profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
