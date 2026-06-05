package com.project.sharist.data.model.ride

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "ride_requests")
data class RideRequestEntity(

    @PrimaryKey
    val id: String,

    @SerialName("passenger_id")
    val passengerId: String,

    @SerialName("departure_latitude")
    val departureLat: Double,
    @SerialName("departure_longitude")
    val departureLng: Double,
    @SerialName("departure_tolerance_radius_meters")
    val departureRadiusMeters: Int,

    @SerialName("arrival_latitude")
    val arrivalLat: Double,
    @SerialName("arrival_longitude")
    val arrivalLng: Double,
    @SerialName("arrival_tolerance_radius_meters")
    val arrivalRadiusMeters: Int,

    @SerialName("departure_time")
    val desiredDepartureTime: String,

    @SerialName("departure_tolerance_minutes")
    val departureToleranceMinutes: Int,

    @SerialName("recurrence_type")
    val recurringType: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    @Transient
    val pendingSync: Boolean = false
)
