package com.project.sharist.data.model.ride

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "ride_requests")
data class RideRequestEntity(

    @PrimaryKey
    val id: String,

    @SerialName("departure_latitude")
    val departureLat: Double,
    @SerialName("departure_longitude")
    val departureLng: Double,
    @SerialName("departure_tolerance_radius_meters")
    val departureRadiusMeters: Double,

    @SerialName("arrival_latitude")
    val arrivalLat: Double,
    @SerialName("arrival_longitude")
    val arrivalLng: Double,
    @SerialName("arrival_tolerance_radius_meters")
    val arrivalRadiusMeters: Double,

    @SerialName("departure_time")
    val desiredDepartureTime: String,

    @SerialName("departure_tolerance_minutes")
    val departureToleranceMinutes: Int,

    @SerialName("recurrence_type")
    val recurringType: String
)
