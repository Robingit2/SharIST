package com.project.sharist.data.model.user

import androidx.room.Entity
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "roles")
data class Role (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: String? = null
)