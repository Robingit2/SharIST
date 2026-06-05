package com.project.sharist.data.model.review

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "user_comments")
data class UserComment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val comment: String,
    @SerialName("rater_user_id")
    val raterUserId : String,
    @SerialName("target_user_id")
    val targetUserId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
