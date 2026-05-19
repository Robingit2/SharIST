package com.project.sharist.ui.screen.ride_request

import com.project.sharist.domain.model.RecurringType
data class RideRequestUiState(
    val departure: String = "",
    val destination: String = "",
    val passengers: Int = 1,
    val recurringType: RecurringType = RecurringType.NONE
)
