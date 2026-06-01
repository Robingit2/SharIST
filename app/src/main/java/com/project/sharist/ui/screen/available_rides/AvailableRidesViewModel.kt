package com.project.sharist.ui.screen.available_rides

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.model.ride.ReservationEntity
import com.project.sharist.data.repository.ReservationRepository
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import com.project.sharist.domain.model.LatLng
import com.project.sharist.domain.model.RecurringType
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.domain.model.RideRequest
import com.project.sharist.ui.util.buildRideOfferTitles
import com.project.sharist.ui.util.launchAddressSearch
import io.github.jan.supabase.auth.auth
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AvailableRidesUiState(
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
    val recurringType: RecurringType = RecurringType.entries.first(),
    val isLoading: Boolean = false,
    val isBooking: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val results: List<RideOffer> = emptyList(),
    val driverNames: Map<String, String> = emptyMap(),
    val rideTitles: Map<String, String> = emptyMap(),
    val reservationCounts: Map<String, Int> = emptyMap()
)

class AvailableRidesViewModel : ViewModel() {
    private val rideOfferRepository: RideOfferRepository = RideOfferRepository()
    private val reservationRepository: ReservationRepository = ReservationRepository()
    private val userRepository: UserRepository = UserRepository()
    private val _uiState = MutableStateFlow(AvailableRidesUiState())
    val uiState: StateFlow<AvailableRidesUiState> = _uiState

    fun updateDepartureAddress(value: String) = _uiState.update { it.copy(departureAddress = value) }
    fun updateDepartureLat(value: String) = _uiState.update { it.copy(departureLat = value) }
    fun updateDepartureLng(value: String) = _uiState.update { it.copy(departureLng = value) }
    fun updateDepartureRadiusMeters(value: String) = _uiState.update { it.copy(departureRadiusMeters = value) }
    fun updateArrivalAddress(value: String) = _uiState.update { it.copy(arrivalAddress = value) }
    fun updateArrivalLat(value: String) = _uiState.update { it.copy(arrivalLat = value) }
    fun updateArrivalLng(value: String) = _uiState.update { it.copy(arrivalLng = value) }
    fun updateArrivalRadiusMeters(value: String) = _uiState.update { it.copy(arrivalRadiusMeters = value) }
    fun updateDesiredDepartureTimeMillis(value: Long) = _uiState.update { it.copy(desiredDepartureTimeMillis = value) }
    fun updateDepartureToleranceMinutes(value: String) = _uiState.update { it.copy(departureToleranceMinutes = value) }
    fun updateRecurringType(value: RecurringType) = _uiState.update { it.copy(recurringType = value) }

    fun bookRide(offerId: String) {
        val passengerId = supabase.auth.currentUserOrNull()?.id

        if (passengerId == null) {
            _uiState.update { it.copy(errorMessage = "No logged in user found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBooking = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                val offer = uiState.value.results.firstOrNull { it.id == offerId }
                val currentReservationCount = reservationRepository.getReservations()
                    .count { it.rideOfferId == offerId }

                if (offer == null || currentReservationCount >= offer.vehicleCapacity) {
                    _uiState.update {
                        it.copy(
                            isBooking = false,
                            errorMessage = "No free spots available."
                        )
                    }
                    return@launch
                }

                when (val result = reservationRepository.insert(ReservationEntity(offerId, passengerId))) {
                    is GenericResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isBooking = false,
                                successMessage = "Ride booked.",
                                reservationCounts = it.reservationCounts + (offerId to currentReservationCount + 1)
                            )
                        }
                    }

                    is GenericResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isBooking = false,
                                errorMessage = result.error.toMessage()
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isBooking = false,
                        errorMessage = exception.message ?: "Could not check available spots."
                    )
                }
            }
        }
    }

    fun searchDepartureAddress(context: Context) {
        launchAddressSearch(
            context = context,
            query = uiState.value.departureAddress,
            onLoadingChange = { isLoading -> _uiState.update { it.copy(isLoading = isLoading) } },
            onError = { error -> _uiState.update { it.copy(errorMessage = error) } },
            onFound = { location ->
                _uiState.update {
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
            query = uiState.value.arrivalAddress,
            onLoadingChange = { isLoading -> _uiState.update { it.copy(isLoading = isLoading) } },
            onError = { error -> _uiState.update { it.copy(errorMessage = error) } },
            onFound = { location ->
                _uiState.update {
                    it.copy(
                        arrivalLat = location.latitude.toString(),
                        arrivalLng = location.longitude.toString(),
                        errorMessage = null
                    )
                }
            }
        )
    }

    fun searchAvailableRides(context: Context) {
        val filter = uiState.value.toRideRequestOrError() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val offers = loadAvailableRides(filter)
                val driverNames = loadDriverNames(offers)
                val rideTitles = buildRideOfferTitles(context, offers)
                val reservationCounts = loadReservationCounts()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = offers,
                        driverNames = driverNames,
                        rideTitles = rideTitles,
                        reservationCounts = reservationCounts
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load available rides."
                    )
                }
            }
        }
    }

    private suspend fun loadAvailableRides(filter: RideRequest): List<RideOffer> {
        return rideOfferRepository.getOffers(filter).map { it.toDomain() }
    }

    private suspend fun loadDriverNames(offers: List<RideOffer>): Map<String, String> {
        return offers
            .map { it.driverId }
            .distinct()
            .mapNotNull { driverId ->
                when (val result = userRepository.getUser(driverId)) {
                    is GenericResult.Success -> driverId to result.data.name
                    is GenericResult.Error -> null
                }
            }
            .toMap()
    }

    private suspend fun loadReservationCounts(): Map<String, Int> {
        return reservationRepository.getReservations()
            .groupingBy { it.rideOfferId }
            .eachCount()
    }

    private fun AvailableRidesUiState.toRideRequestOrError(): RideRequest? {
        val departureLatValue = departureLat.toDoubleOrNull()
        val departureLngValue = departureLng.toDoubleOrNull()
        val departureRadiusValue = departureRadiusMeters.toIntOrNull()
        val arrivalLatValue = arrivalLat.toDoubleOrNull()
        val arrivalLngValue = arrivalLng.toDoubleOrNull()
        val arrivalRadiusValue = arrivalRadiusMeters.toIntOrNull()
        val departureTimeValue = desiredDepartureTimeMillis
        val toleranceValue = departureToleranceMinutes.toIntOrNull()

        val error = when {
            departureLatValue == null || departureLngValue == null -> "Select or enter departure coordinates."
            departureRadiusValue == null || departureRadiusValue <= 0.0 -> "Enter a valid departure radius."
            arrivalLatValue == null || arrivalLngValue == null -> "Select or enter arrival coordinates."
            arrivalRadiusValue == null || arrivalRadiusValue <= 0.0 -> "Enter a valid arrival radius."
            departureTimeValue == null -> "Select a desired departure time."
            toleranceValue == null || toleranceValue < 0 -> "Enter a valid departure tolerance."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return null
        }

        return RideRequest(
            id = UUID.randomUUID().toString(),
            passengerId = supabase.auth.currentUserOrNull()?.id.orEmpty(),
            departure = LatLng(departureLatValue ?: return null, departureLngValue ?: return null),
            departureRadiusMeters = departureRadiusValue ?: return null,
            arrival = LatLng(arrivalLatValue ?: return null, arrivalLngValue ?: return null),
            arrivalRadiusMeters = arrivalRadiusValue ?: return null,
            desiredDepartureTimeMillis = departureTimeValue ?: return null,
            departureToleranceMinutes = toleranceValue ?: return null,
            recurringType = recurringType
        )
    }
}

private fun AppError.toMessage(): String {
    return when (this) {
        AppError.Network -> "Network error while booking ride."
        AppError.Conflict -> "You may already have booked this ride."
        AppError.Unauthorized -> "You are not allowed to book this ride."
        AppError.NotFound -> "Reservation table or ride offer was not found."
        is AppError.Unknown -> message ?: "Could not book ride."
    }
}
