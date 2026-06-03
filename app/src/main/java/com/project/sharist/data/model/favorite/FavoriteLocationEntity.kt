package com.project.sharist.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteLocationEntity(

    @SerialName("id")
    val id: Long? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)


