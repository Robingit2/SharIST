package com.project.sharist.data.model.ride

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
    tableName = "reservations",
    primaryKeys = ["rideOfferId", "passengerId"]
)
data class ReservationEntity (
    @SerialName("ride_offer_id")
    val rideOfferId: String,

    @SerialName("passenger_id")
    val passengerId: String,

    @Transient
    val cacheLastAccessedAtMillis: Long = 0L,
    @Transient
    val cacheFetchedAtMillis: Long = 0L
)
