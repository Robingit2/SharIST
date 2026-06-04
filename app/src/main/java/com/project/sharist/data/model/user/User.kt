package com.project.sharist.data.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// TODO Separate supabase and room entities
@Serializable
@Entity(tableName = "users")
data class User (
    @PrimaryKey val id: String,
    val name: String,
    @SerialName("photo_path")
    val photoPath: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @Transient
    val cacheLastAccessedAtMillis: Long = 0L,
    @Transient
    val cacheFetchedAtMillis: Long = 0L
)
