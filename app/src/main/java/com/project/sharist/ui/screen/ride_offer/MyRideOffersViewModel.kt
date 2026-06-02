package com.project.sharist.ui.screen.ride_offer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.repository.ReservationRepository
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.ui.util.buildRideOfferTitles
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRideOffersUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val offers: List<RideOffer> = emptyList(),
    val rideTitles: Map<String, String> = emptyMap(),
    val reservationCounts: Map<String, Int> = emptyMap(),
    val passengerIdsByOffer: Map<String, List<String>> = emptyMap(),
    val passengerNames: Map<String, String> = emptyMap()
)

class MyRideOffersViewModel(
    private val repository: RideOfferRepository,
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRideOffersUiState())
    val uiState: StateFlow<MyRideOffersUiState> = _uiState.asStateFlow()

    fun loadOffers(context: Context) {
        val driverId = supabase.auth.currentUserOrNull()?.id

        if (driverId == null) {
            _uiState.value = MyRideOffersUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val offers = repository.getOffersByDriver(driverId).map { it.toDomain() }
                val offerIds = offers.map { it.id }.toSet()
                val passengerIdsByOffer = loadPassengerIdsByOffer(offerIds)
                _uiState.value = MyRideOffersUiState(
                    offers = offers,
                    rideTitles = buildRideOfferTitles(context, offers),
                    reservationCounts = passengerIdsByOffer.mapValues { it.value.size },
                    passengerIdsByOffer = passengerIdsByOffer,
                    passengerNames = loadPassengerNames(passengerIdsByOffer.values.flatten())
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load ride offers."
                    )
                }
            }
        }
    }

    fun deleteOffer(offer: RideOffer) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                repository.delete(offer.id)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        offers = it.offers.filterNot { existingOffer -> existingOffer.id == offer.id },
                        rideTitles = it.rideTitles - offer.id,
                        reservationCounts = it.reservationCounts - offer.id,
                        passengerIdsByOffer = it.passengerIdsByOffer - offer.id
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not delete ride offer."
                    )
                }
            }
        }
    }

    private suspend fun loadPassengerIdsByOffer(offerIds: Set<String>): Map<String, List<String>> {
        return reservationRepository.getReservations()
            .filter { it.rideOfferId in offerIds }
            .groupBy(
                keySelector = { it.rideOfferId },
                valueTransform = { it.passengerId }
            )
    }

    private suspend fun loadPassengerNames(passengerIds: List<String>): Map<String, String> {
        return passengerIds
            .distinct()
            .mapNotNull { passengerId ->
                userRepository.getUser(passengerId).getOrNull()?.let { passengerId to it.name }
            }
            .toMap()
    }
}

private fun <T> com.project.sharist.data.model.GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is com.project.sharist.data.model.GenericResult.Success -> data
        is com.project.sharist.data.model.GenericResult.Error -> null
    }
}
