package com.project.sharist.ui.screen.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.ReservationRepository
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.supabase
import com.project.sharist.ui.util.buildRideOfferTitles
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val offers: List<RideOffer> = emptyList(),
    val rideTitles: Map<String, String> = emptyMap(),
    val driverNames: Map<String, String> = emptyMap(),
    val passengerIdsByOffer: Map<String, List<String>> = emptyMap(),
    val passengerNames: Map<String, String> = emptyMap(),
    val isLoadingMore: Boolean = false,
    val hasMoreOffers: Boolean = false
)

class HistoryViewModel(
    private val rideOfferRepository: RideOfferRepository = RideOfferRepository(),
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    private var nextPage = 0
    private var activeBefore: String? = null

    fun loadHistory(context: Context, role: RoleType) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id

        if (currentUserId == null) {
            _uiState.value = HistoryUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val now = System.currentTimeMillis().toTimestampz()
                activeBefore = now
                nextPage = 0
                val offers = loadHistoryPage(currentUserId, role, now, nextPage)
                val passengerIdsByOffer = if (role == RoleType.DRIVER) {
                    loadPassengerIdsByOffer(offers.map { it.id }.toSet())
                } else {
                    emptyMap()
                }
                nextPage += 1

                _uiState.value = HistoryUiState(
                    offers = offers,
                    rideTitles = buildRideOfferTitles(context, offers),
                    driverNames = if (role == RoleType.PASSENGER) loadDriverNames(offers) else emptyMap(),
                    passengerIdsByOffer = passengerIdsByOffer,
                    passengerNames = loadPassengerNames(passengerIdsByOffer.values.flatten()),
                    hasMoreOffers = offers.size == PAGE_SIZE
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load history."
                    )
                }
            }
        }
    }

    fun loadMoreHistory(context: Context, role: RoleType) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return
        val before = activeBefore ?: return
        val state = _uiState.value

        if (state.isLoading || state.isLoadingMore || !state.hasMoreOffers) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }

            try {
                val offers = loadHistoryPage(currentUserId, role, before, nextPage)
                val passengerIdsByOffer = if (role == RoleType.DRIVER) {
                    loadPassengerIdsByOffer(offers.map { it.id }.toSet())
                } else {
                    emptyMap()
                }
                val passengerNames = loadPassengerNames(passengerIdsByOffer.values.flatten())
                val driverNames = if (role == RoleType.PASSENGER) loadDriverNames(offers) else emptyMap()
                nextPage += 1

                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        offers = it.offers + offers,
                        rideTitles = it.rideTitles + buildRideOfferTitles(context, offers),
                        driverNames = it.driverNames + driverNames,
                        passengerIdsByOffer = it.passengerIdsByOffer + passengerIdsByOffer,
                        passengerNames = it.passengerNames + passengerNames,
                        hasMoreOffers = offers.size == PAGE_SIZE
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = exception.message ?: "Could not load more history."
                    )
                }
            }
        }
    }

    private suspend fun loadHistoryPage(userId: String, role: RoleType, before: String, page: Int): List<RideOffer> {
        val from = page * PAGE_SIZE.toLong()
        val to = from + PAGE_SIZE - 1

        return when (role) {
            RoleType.PASSENGER -> loadPassengerHistory(userId, before, from, to)
            RoleType.DRIVER -> rideOfferRepository
                .getPastOffersByDriver(userId, before, from, to)
                .map { it.toDomain() }
        }
    }

    private suspend fun loadPassengerHistory(passengerId: String, before: String, from: Long, to: Long): List<RideOffer> {
        val reservedOfferIds = reservationRepository
            .getReservationsByPassenger(passengerId)
            .map { it.rideOfferId }

        return rideOfferRepository
            .getPastOffersByIds(reservedOfferIds, before, from, to)
            .map { it.toDomain() }
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

    private suspend fun loadPassengerIdsByOffer(offerIds: Set<String>): Map<String, List<String>> {
        return reservationRepository.getReservationsByOffers(offerIds.toList())
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

private const val PAGE_SIZE = 10

private fun Long.toTimestampz(): String {
    return timestampFormat().format(Date(this)).replace("+0000", "+00:00")
}

private fun timestampFormat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

private fun <T> GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is GenericResult.Success -> data
        is GenericResult.Error -> null
    }
}
