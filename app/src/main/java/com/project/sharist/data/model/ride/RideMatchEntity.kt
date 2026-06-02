package com.project.sharist.data.model.ride

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RideMatchEntity(
    @SerialName("ride_request_id")
    val rideRequestId: String,

    @SerialName("ride_offer_id")
    val rideOfferId: String,

    @SerialName("created_at")
    val createdAt: String? = null
)
