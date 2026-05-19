package com.project.sharist.data.model.review

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "user_ratings")
data class UserRating (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val rating: Int,
    val raterUserId: String,
    val targetUserId: String,
    val createdAt: String? = null
)