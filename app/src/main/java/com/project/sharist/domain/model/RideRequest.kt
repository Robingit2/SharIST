package com.project.sharist.domain.model

import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType

data class RideRequest(
    val id: String,

    val departure: LatLng,
    val departureRadiusMeters: Double,

    val arrival: LatLng,
    val arrivalRadiusMeters: Double,

    val desiredDepartureTimeMillis: Long,

    val toleranceBeforeMinutes: Int,
    val toleranceAfterMinutes: Int,

    val passengers: Int,

    val recurringType: RecurringType
)