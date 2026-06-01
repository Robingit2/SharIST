package com.project.sharist.ui.screen.reservations

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.repository.ReservationRepository
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.supabase
import com.project.sharist.ui.util.buildRideOfferTitles
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val reservations: List<RideOffer> = emptyList(),
    val rideTitles: Map<String, String> = emptyMap(),
    val driverNames: Map<String, String> = emptyMap(),
    val reservationCounts: Map<String, Int> = emptyMap()
)

class ReservationsViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val rideOfferRepository: RideOfferRepository = RideOfferRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    fun loadReservations(context: Context) {
        val passengerId = supabase.auth.currentUserOrNull()?.id

        if (passengerId == null) {
            _uiState.value = ReservationsUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val reservedOfferIds = reservationRepository
                    .getReservationsByPassenger(passengerId)
                    .map { it.rideOfferId }
                    .toSet()
                val offers = rideOfferRepository
                    .getOffers()
                    .map { it.toDomain() }
                    .filter { it.id in reservedOfferIds }

                _uiState.value = ReservationsUiState(
                    reservations = offers,
                    rideTitles = buildRideOfferTitles(context, offers),
                    driverNames = loadDriverNames(offers),
                    reservationCounts = loadReservationCounts()
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load reservations."
                    )
                }
            }
        }
    }

    private suspend fun loadDriverNames(offers: List<RideOffer>): Map<String, String> {
        return offers
            .map { it.driverId }
            .distinct()
            .mapNotNull { driverId ->
                userRepository.getUser(driverId).getOrNull()?.let { driverId to it.name }
            }
            .toMap()
    }

    private suspend fun loadReservationCounts(): Map<String, Int> {
        return reservationRepository.getReservations()
            .groupingBy { it.rideOfferId }
            .eachCount()
    }
}

private fun <T> com.project.sharist.data.model.GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is com.project.sharist.data.model.GenericResult.Success -> data
        is com.project.sharist.data.model.GenericResult.Error -> null
    }
}
