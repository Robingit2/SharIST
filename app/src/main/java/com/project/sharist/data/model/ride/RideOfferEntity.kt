package com.project.sharist.data.model.ride

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_offers")
data class RideOfferEntity(

    @PrimaryKey
    val id: String,

    val departureLat: Double,
    val departureLng: Double,

    val arrivalLat: Double,
    val arrivalLng: Double,

    val departureTimeMillis: Long,
    val estimatedArrivalTimeMillis: Long,

    val cost: Double,
    val vehicleCapacity: Int,

    val cancellationWindowMinutes: Int,

    val recurringType: String
)