package com.project.sharist.domain.model

data class RideOffer(
    val id: String,

    val departure: LatLng,
    val arrival: LatLng,

    val departureTimeMillis: Long,
    val estimatedArrivalTimeMillis: Long,

    val cost: Double,
    val vehicleCapacity: Int,

    val cancellationWindowMinutes: Int,

    val recurringType: RecurringType
)