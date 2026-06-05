package com.project.sharist.data.model.ride

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "ride_offers")
data class RideOfferEntity(

    @PrimaryKey
    val id: String,

    @SerialName("driver_id")
    val driverId: String,

    @SerialName("departure_latitude")
    val departureLat: Double,
    @SerialName("departure_longitude")
    val departureLng: Double,

    @SerialName("arrival_latitude")
    val arrivalLat: Double,
    @SerialName("arrival_longitude")
    val arrivalLng: Double,

    @SerialName("departure_time")
    val departureTime: String,
    @SerialName("estimated_arrival_time")
    val estimatedArrivalTime: String,

    val cost: Double,
    @SerialName("capacity")
    val vehicleCapacity: Int,

    @SerialName("tolerance_minutes")
    val cancellationWindowMinutes: Int,

    @SerialName("recurrence_type")
    val recurringType: String,

    @Transient
    val cacheLastAccessedAtMillis: Long = 0L,
    @Transient
    val cacheFetchedAtMillis: Long = 0L
)
