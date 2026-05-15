package com.project.sharist.data.model.user

import androidx.room.Entity
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "vehicles")
data class Vehicle (
    val id: String = UUID.randomUUID().toString(),
    val plate: String,
    val photoPath: String? = null,
    val userId: String,
    val createdAt: String? = null
)