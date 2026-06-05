package com.project.sharist.ui.screen.ride_request

import com.project.sharist.domain.model.RecurringType
import com.project.sharist.data.repository.RideRequestInsertResult

data class RideRequestUiState(
    val editingPendingRequestId: String? = null,
    val editingCreatedAt: String? = null,
    val departureAddress: String = "",
    val departureLat: String = "",
    val departureLng: String = "",
    val departureRadiusMeters: String = "",
    val arrivalAddress: String = "",
    val arrivalLat: String = "",
    val arrivalLng: String = "",
    val arrivalRadiusMeters: String = "",
    val desiredDepartureTimeMillis: Long? = null,
    val departureToleranceMinutes: String = "",
    val recurringType: RecurringType = RecurringType.ONCE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val saveResult: RideRequestInsertResult? = null
)
