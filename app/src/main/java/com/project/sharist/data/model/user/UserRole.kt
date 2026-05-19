package com.project.sharist.data.model.user

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_roles")
data class UserRole (
    val userId: String,
    val roleId: String,
    val createdAt: String? = null
)