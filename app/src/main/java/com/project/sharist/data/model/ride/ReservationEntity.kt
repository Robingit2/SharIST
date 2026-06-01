package com.project.sharist.data.model.ride

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ReservationEntity (
    @SerialName("ride_offer_id")
    val rideOfferId: String,

    @SerialName("passenger_id")
    val passengerId: String
)
