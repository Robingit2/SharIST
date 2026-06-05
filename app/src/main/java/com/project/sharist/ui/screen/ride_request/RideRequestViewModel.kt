package com.project.sharist.ui.screen.ride_request

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.mapper.toEntity
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.toMessage
import com.project.sharist.data.repository.RideRequestInsertResult
import com.project.sharist.data.repository.RideRequestRepository
import com.project.sharist.data.usecase.ride.InsertRideRequestUseCase
import com.project.sharist.domain.model.LatLng
import com.project.sharist.supabase
import com.project.sharist.ui.util.launchAddressSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.project.sharist.domain.model.RecurringType
import com.project.sharist.domain.model.RideRequest
import io.github.jan.supabase.auth.auth
import java.util.UUID
import kotlinx.coroutines.launch

class RideRequestViewModel(
    private val insertRideRequestUseCase: InsertRideRequestUseCase,
    private val rideRequestRepository: RideRequestRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RideRequestUiState())
    val state: StateFlow<RideRequestUiState> = _state

    fun updateDepartureAddress(value: String) = _state.update { it.copy(departureAddress = value, saved = false, saveResult = null) }
    fun updateDepartureLat(value: String) = _state.update { it.copy(departureLat = value, saved = false, saveResult = null) }
    fun updateDepartureLng(value: String) = _state.update { it.copy(departureLng = value, saved = false, saveResult = null) }
    fun updateDepartureRadiusMeters(value: String) = _state.update { it.copy(departureRadiusMeters = value, saved = false, saveResult = null) }
    fun updateArrivalAddress(value: String) = _state.update { it.copy(arrivalAddress = value, saved = false, saveResult = null) }
    fun updateArrivalLat(value: String) = _state.update { it.copy(arrivalLat = value, saved = false, saveResult = null) }
    fun updateArrivalLng(value: String) = _state.update { it.copy(arrivalLng = value, saved = false, saveResult = null) }
    fun updateArrivalRadiusMeters(value: String) = _state.update { it.copy(arrivalRadiusMeters = value, saved = false, saveResult = null) }
    fun updateDesiredDepartureTimeMillis(value: Long) = _state.update { it.copy(desiredDepartureTimeMillis = value, saved = false, saveResult = null) }
    fun updateDepartureToleranceMinutes(value: String) = _state.update { it.copy(departureToleranceMinutes = value, saved = false, saveResult = null) }
    fun updateRecurringType(value: RecurringType) = _state.update { it.copy(recurringType = value, saved = false, saveResult = null) }

    fun loadPendingRequest(requestId: String) {
        if (_state.value.editingPendingRequestId == requestId) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val request = rideRequestRepository.getPendingRequest(requestId)?.toDomain()

            if (request == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Pending ride request was not found."
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    editingPendingRequestId = request.id,
                    editingCreatedAt = request.createdAt,
                    departureLat = request.departure.latitude.toString(),
                    departureLng = request.departure.longitude.toString(),
                    departureRadiusMeters = request.departureRadiusMeters.toString(),
                    arrivalLat = request.arrival.latitude.toString(),
                    arrivalLng = request.arrival.longitude.toString(),
                    arrivalRadiusMeters = request.arrivalRadiusMeters.toString(),
                    desiredDepartureTimeMillis = request.desiredDepartureTimeMillis,
                    departureToleranceMinutes = request.departureToleranceMinutes.toString(),
                    recurringType = request.recurringType,
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
            val request = state.value.toRideRequestOrError() ?: return@launch

            _state.update { it.copy(isLoading = true, errorMessage = null, saved = false, saveResult = null) }

            if (state.value.editingPendingRequestId != null) {
                rideRequestRepository.savePendingRequest(request.toEntity())
                _state.update {
                    it.copy(
                        isLoading = false,
                        saved = true,
                        saveResult = RideRequestInsertResult.PendingSync
                    )
                }
                return@launch
            }

            when (val result = insertRideRequestUseCase(request)) {
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
                        errorMessage = result.error.toMessage("Could not save ride request.")
                    )
                }
            }
        }
    }

    private fun RideRequestUiState.toRideRequestOrError(): RideRequest? {
        val departureLatValue = departureLat.toDoubleOrNull()
        val departureLngValue = departureLng.toDoubleOrNull()
        val departureRadiusValue = departureRadiusMeters.toIntOrNull()
        val arrivalLatValue = arrivalLat.toDoubleOrNull()
        val arrivalLngValue = arrivalLng.toDoubleOrNull()
        val arrivalRadiusValue = arrivalRadiusMeters.toIntOrNull()
        val departureTime = desiredDepartureTimeMillis
        val departureToleranceValue = departureToleranceMinutes.toIntOrNull()
        val passengerId = supabase.auth.currentUserOrNull()?.id

        val error = when {
            passengerId == null -> "No logged in passenger found."
            departureLatValue == null || departureLngValue == null -> "Select or enter departure coordinates."
            departureRadiusValue == null || departureRadiusValue <= 0.0 -> "Enter a valid departure radius."
            arrivalLatValue == null || arrivalLngValue == null -> "Select or enter arrival coordinates."
            arrivalRadiusValue == null || arrivalRadiusValue <= 0.0 -> "Enter a valid arrival radius."
            departureTime == null -> "Select a departure time."
            departureToleranceValue == null || departureToleranceValue < 0 -> "Enter a valid departure tolerance."
            else -> null
        }

        if (error != null) {
            _state.update { it.copy(errorMessage = error) }
            return null
        }

        return RideRequest(
            id = editingPendingRequestId ?: UUID.randomUUID().toString(),
            passengerId = passengerId ?: return null,
            departure = LatLng(departureLatValue ?: return null, departureLngValue ?: return null),
            departureRadiusMeters = departureRadiusValue ?: return null,
            arrival = LatLng(arrivalLatValue ?: return null, arrivalLngValue ?: return null),
            arrivalRadiusMeters = arrivalRadiusValue ?: return null,
            desiredDepartureTimeMillis = departureTime ?: return null,
            departureToleranceMinutes = departureToleranceValue ?: return null,
            recurringType = recurringType,
            createdAt = editingCreatedAt
        )
    }
}
