package com.project.sharist.data.model.user

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "vehicles")
data class Vehicle (
    val id: String = UUID.randomUUID().toString(),
    val plate: String,
    @SerialName("photo_path")
    val photoPath: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
