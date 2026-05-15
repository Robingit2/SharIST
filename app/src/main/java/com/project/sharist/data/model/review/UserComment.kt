package com.project.sharist.data.model.review

import androidx.room.Entity
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "user_comments")
data class UserComment(
    val id: String = UUID.randomUUID().toString(),
    val comment: String,
    val raterUserId : String,
    val targetUserId: String,
    val createdAt: String? = null
)
