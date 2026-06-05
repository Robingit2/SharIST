package com.project.sharist.ui.screen.ride_offer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.mapper.toEntity
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.toMessage
import com.project.sharist.data.repository.RideOfferInsertResult
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.usecase.ride.InsertRideOfferUseCase
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.supabase
import com.project.sharist.ui.util.launchAddressSearch
import io.github.jan.supabase.auth.auth
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RideOfferViewModel(
    private val insertRideOfferUseCase: InsertRideOfferUseCase,
    private val rideOfferRepository: RideOfferRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RideOfferUiState())
    val state: StateFlow<RideOfferUiState> = _state

    fun updateDepartureAddress(value: String) = _state.update { it.copy(departureAddress = value, saved = false, saveResult = null) }
    fun updateDepartureLat(value: String) = _state.update { it.copy(departureLat = value, saved = false, saveResult = null) }
    fun updateDepartureLng(value: String) = _state.update { it.copy(departureLng = value, saved = false, saveResult = null) }
    fun updateArrivalAddress(value: String) = _state.update { it.copy(arrivalAddress = value, saved = false, saveResult = null) }
    fun updateArrivalLat(value: String) = _state.update { it.copy(arrivalLat = value, saved = false, saveResult = null) }
    fun updateArrivalLng(value: String) = _state.update { it.copy(arrivalLng = value, saved = false, saveResult = null) }
    fun updateDepartureTimeMillis(value: Long) {
        _state.update {
            it.copy(
                departureTimeMillis = value,
                estimatedArrivalTimeMillis = it.estimatedArrivalTimeMillis?.takeIf { arrival ->
                    arrival > value
                },
                saved = false,
                saveResult = null
            )
        }
    }
    fun updateEstimatedArrivalTimeMillis(value: Long) = _state.update { it.copy(estimatedArrivalTimeMillis = value, saved = false, saveResult = null) }
    fun updateCost(value: String) = _state.update { it.copy(cost = value, saved = false, saveResult = null) }
    fun updateVehicleCapacity(value: String) = _state.update { it.copy(vehicleCapacity = value, saved = false, saveResult = null) }
    fun updateCancellationWindowMinutes(value: String) = _state.update { it.copy(cancellationWindowMinutes = value, saved = false, saveResult = null) }
    fun updateRecurringType(value: RecurringType) = _state.update { it.copy(recurringType = value, saved = false, saveResult = null) }

    fun loadPendingOffer(offerId: String) {
        if (_state.value.editingPendingOfferId == offerId) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val offer = rideOfferRepository.getPendingOffer(offerId)?.toDomain()

            if (offer == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Pending ride offer was not found."
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    editingPendingOfferId = offer.id,
                    departureLat = offer.departure.latitude.toString(),
                    departureLng = offer.departure.longitude.toString(),
                    arrivalLat = offer.arrival.latitude.toString(),
                    arrivalLng = offer.arrival.longitude.toString(),
                    departureTimeMillis = offer.departureTimeMillis,
                    estimatedArrivalTimeMillis = offer.estimatedArrivalTimeMillis,
                    cost = offer.cost.toString(),
                    vehicleCapacity = offer.vehicleCapacity.toString(),
                    cancellationWindowMinutes = offer.cancellationWindowMinutes.toString(),
                    recurringType = offer.recurringType,
                    isLoading = false,
                    errorMessage = null,
                    saved = false,
                    saveResult = null
                )
            }
        }
    }

    fun searchDepartureAddress(context: Context) {
        launchAddressSearch(
            context = context,
            query = state.value.departureAddress,
            onLoadingChange = { isLoading -> _state.update { it.copy(isLoading = isLoading) } },
            onError = { error -> _state.update { it.copy(errorMessage = error) } },
            onFound = { location ->
                _state.update {
                    it.copy(
                        departureLat = location.latitude.toString(),
                        departureLng = location.longitude.toString(),
                        errorMessage = null
                    )
                }
            }
        )
    }

    fun searchArrivalAddress(context: Context) {
        launchAddressSearch(
            context = context,
            query = state.value.arrivalAddress,
            onLoadingChange = { isLoading -> _state.update { it.copy(isLoading = isLoading) } },
            onError = { error -> _state.update { it.copy(errorMessage = error) } },
            onFound = { location ->
                _state.update {
                    it.copy(
                        arrivalLat = location.latitude.toString(),
                        arrivalLng = location.longitude.toString(),
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

            _state.update { it.copy(isLoading = true, errorMessage = null, saved = false, saveResult = null) }

            if (state.value.editingPendingOfferId != null) {
                rideOfferRepository.savePendingOffer(offer.toEntity())
                _state.update {
                    it.copy(
                        isLoading = false,
                        saved = true,
                        saveResult = RideOfferInsertResult.PendingSync
                    )
                }
                return@launch
            }

            when (val result = insertRideOfferUseCase(offer)) {
                is GenericResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        saved = true,
                        saveResult = result.data
                    )
                }
                is GenericResult.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.error.toMessage("Could not save ride offer.")
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
            id = editingPendingOfferId ?: UUID.randomUUID().toString(),
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
