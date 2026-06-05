package com.project.sharist.domain.model

import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType

data class RideRequest(
    val id: String,
    val passengerId: String,

    val departure: LatLng,
    val departureRadiusMeters: Int,

    val arrival: LatLng,
    val arrivalRadiusMeters: Int,

    val desiredDepartureTimeMillis: Long,
    val departureToleranceMinutes: Int,

    val recurringType: RecurringType,
    val createdAt: String? = null
)
