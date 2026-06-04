package com.project.sharist.ui.screen.ride_request

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.model.ride.ReservationEntity
import com.project.sharist.data.repository.ReservationRepository
import com.project.sharist.data.repository.RideMatchRepository
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.RideRequestRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.domain.model.RideRequest
import com.project.sharist.supabase
import com.project.sharist.ui.util.buildRideOfferTitles
import com.project.sharist.ui.util.buildRideRequestTitles
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRideRequestsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val requests: List<RideRequest> = emptyList(),
    val rideTitles: Map<String, String> = emptyMap(),
    val expandedRequestId: String? = null,
    val loadingMatchesRequestId: String? = null,
    val loadingMoreMatchesRequestId: String? = null,
    val bookingOfferId: String? = null,
    val matchErrorMessage: String? = null,
    val matchedOffers: Map<String, List<RideOffer>> = emptyMap(),
    val matchedOfferTitles: Map<String, String> = emptyMap(),
    val driverNames: Map<String, String> = emptyMap(),
    val reservationCounts: Map<String, Int> = emptyMap(),
    val isLoadingMoreRequests: Boolean = false,
    val hasMoreRequests: Boolean = false,
    val hasMoreMatchesByRequest: Map<String, Boolean> = emptyMap()
)

class MyRideRequestsViewModel(
    private val repository: RideRequestRepository = RideRequestRepository(),
    private val rideMatchRepository: RideMatchRepository = RideMatchRepository(),
    private val rideOfferRepository: RideOfferRepository,
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRideRequestsUiState())
    val uiState: StateFlow<MyRideRequestsUiState> = _uiState.asStateFlow()
    private var nextRequestPage = 0
    private var activeRequestsAfter: String? = null
    private val nextMatchPageByRequest = mutableMapOf<String, Int>()

    fun loadRequests(context: Context) {
        val passengerId = supabase.auth.currentUserOrNull()?.id

        if (passengerId == null) {
            _uiState.value = MyRideRequestsUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val after = System.currentTimeMillis().toTimestampz()
                activeRequestsAfter = after
                nextRequestPage = 0
                nextMatchPageByRequest.clear()
                val requests = loadRequestsPage(passengerId, after, nextRequestPage)
                nextRequestPage += 1

                _uiState.value = MyRideRequestsUiState(
                    requests = requests,
                    rideTitles = buildRideRequestTitles(context, requests),
                    hasMoreRequests = requests.size == PAGE_SIZE
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load ride requests."
                    )
                }
            }
        }
    }

    fun loadMoreRequests(context: Context) {
        val passengerId = supabase.auth.currentUserOrNull()?.id ?: return
        val after = activeRequestsAfter ?: return
        val state = _uiState.value

        if (state.isLoading || state.isLoadingMoreRequests || !state.hasMoreRequests) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreRequests = true, errorMessage = null) }

            try {
                val requests = loadRequestsPage(passengerId, after, nextRequestPage)
                nextRequestPage += 1

                _uiState.update {
                    it.copy(
                        isLoadingMoreRequests = false,
                        requests = it.requests + requests,
                        rideTitles = it.rideTitles + buildRideRequestTitles(context, requests),
                        hasMoreRequests = requests.size == PAGE_SIZE
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMoreRequests = false,
                        errorMessage = exception.message ?: "Could not load more ride requests."
                    )
                }
            }
        }
    }

    fun toggleMatches(context: Context, requestId: String) {
        if (_uiState.value.expandedRequestId == requestId) {
            _uiState.update { it.copy(expandedRequestId = null, matchErrorMessage = null) }
            return
        }

        val cachedMatches = _uiState.value.matchedOffers[requestId]
        if (cachedMatches != null) {
            _uiState.update {
                it.copy(
                    expandedRequestId = requestId,
                    matchErrorMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    expandedRequestId = requestId,
                    loadingMatchesRequestId = requestId,
                    matchErrorMessage = null
                )
            }

            try {
                nextMatchPageByRequest[requestId] = 0
                val matchPage = loadMatchesPage(requestId, nextMatchPageByRequest.getValue(requestId))
                nextMatchPageByRequest[requestId] = nextMatchPageByRequest.getValue(requestId) + 1
                val offers = matchPage.offers
                val offerTitles = buildRideOfferTitles(context, offers)
                val driverNames = loadDriverNames(offers)
                val reservationCounts = loadReservationCounts(offers)

                _uiState.update {
                    it.copy(
                        loadingMatchesRequestId = null,
                        matchedOffers = it.matchedOffers + (requestId to offers),
                        matchedOfferTitles = it.matchedOfferTitles + offerTitles,
                        driverNames = it.driverNames + driverNames,
                        reservationCounts = it.reservationCounts + reservationCounts,
                        hasMoreMatchesByRequest = it.hasMoreMatchesByRequest + (requestId to matchPage.hasMore)
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        loadingMatchesRequestId = null,
                        matchErrorMessage = exception.message ?: "Could not load matches."
                    )
                }
            }
        }
    }

    fun loadMoreMatches(context: Context, requestId: String) {
        val state = _uiState.value

        if (
            state.loadingMatchesRequestId == requestId ||
            state.loadingMoreMatchesRequestId == requestId ||
            state.hasMoreMatchesByRequest[requestId] != true
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingMoreMatchesRequestId = requestId,
                    matchErrorMessage = null
                )
            }

            try {
                val page = nextMatchPageByRequest[requestId] ?: 0
                val matchPage = loadMatchesPage(requestId, page)
                nextMatchPageByRequest[requestId] = page + 1
                val offers = matchPage.offers
                val offerTitles = buildRideOfferTitles(context, offers)
                val driverNames = loadDriverNames(offers)
                val reservationCounts = loadReservationCounts(offers)

                _uiState.update {
                    it.copy(
                        loadingMoreMatchesRequestId = null,
                        matchedOffers = it.matchedOffers + (requestId to (it.matchedOffers[requestId].orEmpty() + offers)),
                        matchedOfferTitles = it.matchedOfferTitles + offerTitles,
                        driverNames = it.driverNames + driverNames,
                        reservationCounts = it.reservationCounts + reservationCounts,
                        hasMoreMatchesByRequest = it.hasMoreMatchesByRequest + (requestId to matchPage.hasMore)
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        loadingMoreMatchesRequestId = null,
                        matchErrorMessage = exception.message ?: "Could not load more matches."
                    )
                }
            }
        }
    }

    fun bookMatchedRide(requestId: String, offerId: String) {
        val passengerId = supabase.auth.currentUserOrNull()?.id

        if (passengerId == null) {
            _uiState.update { it.copy(matchErrorMessage = "No logged in user found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    bookingOfferId = offerId,
                    matchErrorMessage = null
                )
            }

            try {
                val offer = _uiState.value.matchedOffers[requestId]
                    ?.firstOrNull { it.id == offerId }
                val currentReservationCount = reservationRepository.getReservationCountByOffer(offerId)

                if (offer == null || currentReservationCount >= offer.vehicleCapacity) {
                    _uiState.update {
                        it.copy(
                            bookingOfferId = null,
                            matchErrorMessage = "No free spots available."
                        )
                    }
                    return@launch
                }

                when (val result = reservationRepository.insert(ReservationEntity(offerId, passengerId))) {
                    is GenericResult.Success -> {
                        repository.delete(requestId)
                        nextMatchPageByRequest.remove(requestId)
                        _uiState.update {
                            it.copy(
                                bookingOfferId = null,
                                expandedRequestId = null,
                                requests = it.requests.filterNot { request -> request.id == requestId },
                                rideTitles = it.rideTitles - requestId,
                                matchedOffers = it.matchedOffers - requestId,
                                hasMoreMatchesByRequest = it.hasMoreMatchesByRequest - requestId,
                                reservationCounts = it.reservationCounts + (offerId to currentReservationCount + 1),
                                matchErrorMessage = null
                            )
                        }
                    }

                    is GenericResult.Error -> {
                        _uiState.update {
                            it.copy(
                                bookingOfferId = null,
                                matchErrorMessage = result.error.toMessage()
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        bookingOfferId = null,
                        matchErrorMessage = exception.message ?: "Could not book ride."
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

    private suspend fun loadReservationCounts(offers: List<RideOffer>): Map<String, Int> {
        return reservationRepository.getReservationCountsByOffers(offers.map { it.id })
    }

    private suspend fun loadRequestsPage(passengerId: String, after: String, page: Int): List<RideRequest> {
        val from = page * PAGE_SIZE.toLong()
        val to = from + PAGE_SIZE - 1

        return repository
            .getFutureRequestsByPassenger(passengerId, after, from, to)
            .map { it.toDomain() }
    }

    private suspend fun loadMatchesPage(requestId: String, page: Int): MatchPage {
        val from = page * PAGE_SIZE.toLong()
        val to = from + PAGE_SIZE - 1
        val matches = rideMatchRepository.getMatchesByRequest(requestId, from, to)
        val offerIds = matches.map { it.rideOfferId }
        val offers = rideOfferRepository
            .getFutureOffersByIds(offerIds, System.currentTimeMillis().toTimestampz())
            .map { it.toDomain() }

        return MatchPage(
            offers = offers,
            hasMore = matches.size == PAGE_SIZE
        )
    }
}

private data class MatchPage(
    val offers: List<RideOffer>,
    val hasMore: Boolean
)

private const val PAGE_SIZE = 10

private fun <T> com.project.sharist.data.model.GenericResult<T>.getOrNull(): T? {
    return when (this) {
        is com.project.sharist.data.model.GenericResult.Success -> data
        is com.project.sharist.data.model.GenericResult.Error -> null
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

private fun Long.toTimestampz(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(java.util.Date(this))
        .replace("+0000", "+00:00")
}
