package com.project.sharist.data.model

import kotlinx.serialization.Serializable

// TODO Create ROOM model for local storage
@Serializable
data class User (
    val id: String,
    val name: String,
    val photoPath: String
)