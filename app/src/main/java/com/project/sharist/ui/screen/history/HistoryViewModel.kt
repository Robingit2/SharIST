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
    val passengerNames: Map<String, String> = emptyMap()
)

class HistoryViewModel(
    private val rideOfferRepository: RideOfferRepository = RideOfferRepository(),
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

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
                val offers = when (role) {
                    RoleType.PASSENGER -> loadPassengerHistory(currentUserId, System.currentTimeMillis())
                    RoleType.DRIVER -> rideOfferRepository
                        .getPastOffersByDriver(currentUserId, now)
                        .map { it.toDomain() }
                }
                val passengerIdsByOffer = if (role == RoleType.DRIVER) {
                    loadPassengerIdsByOffer(offers.map { it.id }.toSet())
                } else {
                    emptyMap()
                }

                _uiState.value = HistoryUiState(
                    offers = offers,
                    rideTitles = buildRideOfferTitles(context, offers),
                    driverNames = if (role == RoleType.PASSENGER) loadDriverNames(offers) else emptyMap(),
                    passengerIdsByOffer = passengerIdsByOffer,
                    passengerNames = loadPassengerNames(passengerIdsByOffer.values.flatten())
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

    private suspend fun loadPassengerHistory(passengerId: String, nowMillis: Long): List<RideOffer> {
        val reservedOfferIds = reservationRepository
            .getReservationsByPassenger(passengerId)
            .map { it.rideOfferId }
            .toSet()

        return rideOfferRepository
            .getOffers()
            .map { it.toDomain() }
            .filter { it.id in reservedOfferIds && it.departureTimeMillis < nowMillis }
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
