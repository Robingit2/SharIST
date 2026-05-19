package com.project.sharist.data.model.ride

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_requests")
data class RideRequestEntity(

    @PrimaryKey
    val id: String,

    val departureLat: Double,
    val departureLng: Double,
    val departureRadiusMeters: Double,

    val arrivalLat: Double,
    val arrivalLng: Double,
    val arrivalRadiusMeters: Double,

    val desiredDepartureTimeMillis: Long,

    val toleranceBeforeMinutes: Int,
    val toleranceAfterMinutes: Int,

    val passengers: Int,

    val recurringType: String
)