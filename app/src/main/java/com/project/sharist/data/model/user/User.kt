package com.project.sharist.data.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO Separate supabase and room entities
@Serializable
@Entity(tableName = "users")
data class User (
    @PrimaryKey val id: String,
    val name: String,
    @SerialName("photo_path")
    val photoPath: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)