package com.project.sharist.ui.screen.ride_offer

import com.project.sharist.domain.model.RecurringType

data class RideOfferUiState(
    val departureAddress: String = "",
    val departureLat: String = "",
    val departureLng: String = "",
    val arrivalAddress: String = "",
    val arrivalLat: String = "",
    val arrivalLng: String = "",
    val departureTimeMillis: Long? = null,
    val estimatedArrivalTimeMillis: Long? = null,
    val cost: String = "",
    val vehicleCapacity: String = "",
    val cancellationWindowMinutes: String = "",
    val recurringType: RecurringType = RecurringType.NONE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
)
