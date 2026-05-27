package com.project.sharist.data.model.user

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_roles")
data class UserRole (
    @SerialName("user_id")
    val userId: String,
    @SerialName("role_id")
    val roleId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
