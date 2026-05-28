package com.project.sharist.ui.screen.ride_offer

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.usecase.ride.InsertRideOfferUseCase
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RideOfferViewModel(
    private val insertRideOfferUseCase: InsertRideOfferUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RideOfferUiState())
    val state: StateFlow<RideOfferUiState> = _state

    fun updateDepartureAddress(value: String) = _state.update { it.copy(departureAddress = value, saved = false) }
    fun updateDepartureLat(value: String) = _state.update { it.copy(departureLat = value, saved = false) }
    fun updateDepartureLng(value: String) = _state.update { it.copy(departureLng = value, saved = false) }
    fun updateArrivalAddress(value: String) = _state.update { it.copy(arrivalAddress = value, saved = false) }
    fun updateArrivalLat(value: String) = _state.update { it.copy(arrivalLat = value, saved = false) }
    fun updateArrivalLng(value: String) = _state.update { it.copy(arrivalLng = value, saved = false) }
    fun updateDepartureTimeMillis(value: Long) = _state.update { it.copy(departureTimeMillis = value, saved = false) }
    fun updateEstimatedArrivalTimeMillis(value: Long) = _state.update { it.copy(estimatedArrivalTimeMillis = value, saved = false) }
    fun updateCost(value: String) = _state.update { it.copy(cost = value, saved = false) }
    fun updateVehicleCapacity(value: String) = _state.update { it.copy(vehicleCapacity = value, saved = false) }
    fun updateCancellationWindowMinutes(value: String) = _state.update { it.copy(cancellationWindowMinutes = value, saved = false) }
    fun updateRecurringType(value: RecurringType) = _state.update { it.copy(recurringType = value, saved = false) }

    fun searchDepartureAddress(context: Context) {
        searchAddress(
            context = context,
            query = state.value.departureAddress,
            onFound = { lat, lng ->
                _state.update {
                    it.copy(
                        departureLat = lat.toString(),
                        departureLng = lng.toString(),
                        errorMessage = null
                    )
                }
            }
        )
    }

    fun searchArrivalAddress(context: Context) {
        searchAddress(
            context = context,
            query = state.value.arrivalAddress,
            onFound = { lat, lng ->
                _state.update {
                    it.copy(
                        arrivalLat = lat.toString(),
                        arrivalLng = lng.toString(),
                        errorMessage = null
                    )
                }
            }
        )
    }

    fun save() {
        viewModelScope.launch {
            val offer = state.value.toRideOfferOrError()

            if (offer == null) {
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null, saved = false) }

            try {
                insertRideOfferUseCase(offer)
                _state.update { it.copy(isLoading = false, saved = true) }
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not save ride offer."
                    )
                }
            }
        }
    }

    private fun searchAddress(
        context: Context,
        query: String,
        onFound: (lat: Double, lng: Double) -> Unit
    ) {
        if (query.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter an address to search.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = withContext(Dispatchers.IO) {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocationName(query, 1)
                        ?.firstOrNull()
                }

                if (result == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Address not found."
                        )
                    }
                } else {
                    onFound(result.latitude, result.longitude)
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not search address."
                    )
                }
            }
        }
    }

    private fun RideOfferUiState.toRideOfferOrError(): RideOffer? {
        val departureLatValue = departureLat.toDoubleOrNull()
        val departureLngValue = departureLng.toDoubleOrNull()
        val arrivalLatValue = arrivalLat.toDoubleOrNull()
        val arrivalLngValue = arrivalLng.toDoubleOrNull()
        val departureTime = departureTimeMillis
        val arrivalTime = estimatedArrivalTimeMillis
        val costValue = cost.toDoubleOrNull()
        val capacityValue = vehicleCapacity.toIntOrNull()
        val cancellationWindowValue = cancellationWindowMinutes.toIntOrNull()
        val driverId = supabase.auth.currentUserOrNull()?.id

        val error = when {
            driverId == null -> "No logged in driver found."
            departureLatValue == null || departureLngValue == null -> "Select or enter departure coordinates."
            arrivalLatValue == null || arrivalLngValue == null -> "Select or enter arrival coordinates."
            departureTime == null -> "Select a departure time."
            arrivalTime == null -> "Select an estimated arrival time."
            arrivalTime <= departureTime -> "Estimated arrival must be after departure."
            costValue == null || costValue < 0.0 -> "Enter a valid cost."
            capacityValue == null || capacityValue <= 0 -> "Enter a valid vehicle capacity."
            cancellationWindowValue == null || cancellationWindowValue < 0 -> "Enter a valid cancellation window."
            else -> null
        }

        if (error != null) {
            _state.update { it.copy(errorMessage = error) }
            return null
        }

        val validatedDepartureLat = departureLatValue ?: return null
        val validatedDepartureLng = departureLngValue ?: return null
        val validatedArrivalLat = arrivalLatValue ?: return null
        val validatedArrivalLng = arrivalLngValue ?: return null
        val validatedDepartureTime = departureTime ?: return null
        val validatedArrivalTime = arrivalTime ?: return null
        val validatedCost = costValue ?: return null
        val validatedCapacity = capacityValue ?: return null
        val validatedCancellationWindow = cancellationWindowValue ?: return null
        val validatedDriverId = driverId ?: return null

        return RideOffer(
            id = UUID.randomUUID().toString(),
            driverId = validatedDriverId,
            departure = LatLng(validatedDepartureLat, validatedDepartureLng),
            arrival = LatLng(validatedArrivalLat, validatedArrivalLng),
            departureTimeMillis = validatedDepartureTime,
            estimatedArrivalTimeMillis = validatedArrivalTime,
            cost = validatedCost,
            vehicleCapacity = validatedCapacity,
            cancellationWindowMinutes = validatedCancellationWindow,
            recurringType = recurringType
        )
    }
}
