package com.project.sharist.data.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Instant

// TODO Separate supabase and room entities
@Serializable
@Entity(tableName = "users")
data class User (
    @PrimaryKey val id: String,
    val name: String,
    val photoPath: String? = null,
    val createdAt: Instant? = null
)