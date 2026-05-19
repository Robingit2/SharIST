package com.project.sharist.ui.screen.ride_request

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.project.sharist.domain.model.RecurringType

class RideRequestViewModel : ViewModel() {
    private val _state = MutableStateFlow(RideRequestUiState())
    val state: StateFlow<RideRequestUiState> = _state

    fun updateDeparture(value: String) {
        _state.update {
            it.copy(departure = value)
        }
    }

    fun updateDestination(value: String) {
        _state.update {
            it.copy(destination = value)
        }
    }

    fun updatePassengers(value: Int) {
        _state.update {
            it.copy(passengers = value)
        }
    }

    fun updateRecurring(type: RecurringType) {
        _state.update {
            it.copy(recurringType = type)
        }
    }
}