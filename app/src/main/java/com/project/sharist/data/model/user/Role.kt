package com.project.sharist.data.model.user

import java.util.UUID

data class Role (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: String? = null
)