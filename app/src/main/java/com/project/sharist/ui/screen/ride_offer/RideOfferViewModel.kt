package com.project.sharist.ui.screen.ride_offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.project.sharist.domain.model.RideOffer
import com.project.sharist.domain.model.LatLng
import com.project.sharist.data.usecase.ride.InsertRideOfferUseCase



class RideOfferViewModel: ViewModel() {
    private val _state = MutableStateFlow(RideOfferUiState())
    val state: StateFlow<RideOfferUiState> = _state

    fun updatePickup(value: String) {
        _state.update { it.copy(pickup = value) }
    }

    fun updateDestination(value: String) {
        _state.update { it.copy(destination = value) }
    }

    fun updateCost(value: String) {
        _state.update { it.copy(cost = value) }
    }

    fun save() {
        viewModelScope.launch {

            val domainModel = RideOffer(
                id = "",
                departure = LatLng(0.0, 0.0),   // later map properly
                arrival = LatLng(0.0, 0.0),
                departureTimeMillis = System.currentTimeMillis(),
                estimatedArrivalTimeMillis = System.currentTimeMillis(),
                cost = state.value.cost.toDoubleOrNull() ?: 0.0,
                vehicleCapacity = 4,
                cancellationWindowMinutes = 30,
                recurringType = com.project.sharist.domain.model.RecurringType.NONE
            )
        }
    }
}