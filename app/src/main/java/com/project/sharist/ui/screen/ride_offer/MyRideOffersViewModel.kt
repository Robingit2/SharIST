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
    val passengerNames: Map<String, String> = emptyMap(),
    val isLoadingMore: Boolean = false,
    val hasMoreOffers: Boolean = false
)

class MyRideOffersViewModel(
    private val repository: RideOfferRepository,
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRideOffersUiState())
    val uiState: StateFlow<MyRideOffersUiState> = _uiState.asStateFlow()
    private var nextPage = 0

    fun loadOffers(context: Context) {
        val driverId = supabase.auth.currentUserOrNull()?.id

        if (driverId == null) {
            _uiState.value = MyRideOffersUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                nextPage = 0
                val offers = loadOffersPage(driverId, nextPage)
                val passengerIdsByOffer = loadPassengerIdsByOffer(offers.map { it.id }.toSet())
                nextPage += 1
                _uiState.value = MyRideOffersUiState(
                    offers = offers,
                    rideTitles = buildRideOfferTitles(context, offers),
                    reservationCounts = passengerIdsByOffer.mapValues { it.value.size },
                    passengerIdsByOffer = passengerIdsByOffer,
                    passengerNames = loadPassengerNames(passengerIdsByOffer.values.flatten()),
                    hasMoreOffers = offers.size == PAGE_SIZE
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

    fun loadMoreOffers(context: Context) {
        val driverId = supabase.auth.currentUserOrNull()?.id ?: return
        val state = _uiState.value

        if (state.isLoading || state.isLoadingMore || !state.hasMoreOffers) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }

            try {
                val offers = loadOffersPage(driverId, nextPage)
                val passengerIdsByOffer = loadPassengerIdsByOffer(offers.map { it.id }.toSet())
                val passengerNames = loadPassengerNames(passengerIdsByOffer.values.flatten())
                nextPage += 1

                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        offers = it.offers + offers,
                        rideTitles = it.rideTitles + buildRideOfferTitles(context, offers),
                        reservationCounts = it.reservationCounts + passengerIdsByOffer.mapValues { entry -> entry.value.size },
                        passengerIdsByOffer = it.passengerIdsByOffer + passengerIdsByOffer,
                        passengerNames = it.passengerNames + passengerNames,
                        hasMoreOffers = offers.size == PAGE_SIZE
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = exception.message ?: "Could not load more ride offers."
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
        return reservationRepository.getReservationsByOffers(offerIds.toList())
            .groupBy(
                keySelector = { it.rideOfferId },
                valueTransform = { it.passengerId }
            )
    }

    private suspend fun loadOffersPage(driverId: String, page: Int): List<RideOffer> {
        val from = page * PAGE_SIZE.toLong()
        val to = from + PAGE_SIZE - 1

        return repository
            .getFutureOffersByDriver(driverId, System.currentTimeMillis().toTimestampz(), from, to)
            .map { it.toDomain() }
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

private const val PAGE_SIZE = 10

private fun <T> com.project.sharist.data.model.GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is com.project.sharist.data.model.GenericResult.Success -> data
        is com.project.sharist.data.model.GenericResult.Error -> null
    }
}

private fun Long.toTimestampz(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(java.util.Date(this))
        .replace("+0000", "+00:00")
}
