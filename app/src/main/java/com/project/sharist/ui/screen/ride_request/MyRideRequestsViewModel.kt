package com.project.sharist.ui.screen.ride_request

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.getOrNull
import com.project.sharist.data.model.getOrThrow
import com.project.sharist.data.model.toMessage
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.mapper.toEntity
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
    val pendingSyncRequestIds: Set<String> = emptySet(),
    val syncingRequestId: String? = null,
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

            val after = System.currentTimeMillis().toTimestampz()
            activeRequestsAfter = after
            nextRequestPage = 0
            nextMatchPageByRequest.clear()

            try {
                val pendingRequests = repository
                    .getPendingFutureRequestsByPassenger(passengerId, after)
                    .map { it.toDomain() }
                val syncedRequests = loadRequestsPage(passengerId, after, nextRequestPage)
                val requests = mergePendingAndSyncedRequests(pendingRequests, syncedRequests)
                nextRequestPage += 1

                _uiState.value = MyRideRequestsUiState(
                    requests = requests,
                    rideTitles = buildRideRequestTitles(context, requests),
                    pendingSyncRequestIds = pendingRequests.map { it.id }.toSet(),
                    hasMoreRequests = syncedRequests.size == PAGE_SIZE
                )
            } catch (exception: Exception) {
                val pendingRequests = repository
                    .getPendingFutureRequestsByPassenger(passengerId, after)
                    .map { it.toDomain() }

                if (pendingRequests.isNotEmpty()) {
                    _uiState.value = MyRideRequestsUiState(
                        requests = pendingRequests,
                        rideTitles = buildRideRequestTitles(context, pendingRequests),
                        pendingSyncRequestIds = pendingRequests.map { it.id }.toSet(),
                        errorMessage = null
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Could not load ride requests."
                        )
                    }
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
        if (requestId in _uiState.value.pendingSyncRequestIds) {
            _uiState.update { it.copy(matchErrorMessage = "Send this request to the server before viewing matches.") }
            return
        }

        val passengerId = supabase.auth.currentUserOrNull()?.id

        if (passengerId == null) {
            _uiState.update { it.copy(matchErrorMessage = "No logged in user found.") }
            return
        }

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
                val matchPage = loadMatchesPage(requestId, passengerId, nextMatchPageByRequest.getValue(requestId))
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

    fun syncPendingRequest(context: Context, requestId: String) {
        val request = _uiState.value.requests.firstOrNull { it.id == requestId } ?: return

        if (requestId !in _uiState.value.pendingSyncRequestIds || _uiState.value.syncingRequestId != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    syncingRequestId = requestId,
                    errorMessage = null,
                    matchErrorMessage = null
                )
            }

            when (val result = repository.syncPendingRequest(request.toEntity())) {
                is GenericResult.Success -> {
                    _uiState.update {
                        it.copy(
                            syncingRequestId = null,
                            pendingSyncRequestIds = it.pendingSyncRequestIds - requestId,
                            rideTitles = it.rideTitles + buildRideRequestTitles(context, listOf(request)),
                            errorMessage = null
                        )
                    }
                }

                is GenericResult.Error -> {
                    _uiState.update {
                        it.copy(
                            syncingRequestId = null,
                            errorMessage = result.error.toMessage("Could not send ride request.")
                        )
                    }
                }
            }
        }
    }

    fun deletePendingRequest(requestId: String) {
        if (requestId !in _uiState.value.pendingSyncRequestIds || _uiState.value.syncingRequestId == requestId) {
            return
        }

        viewModelScope.launch {
            rideRequestRepositoryDeletePending(requestId)
            nextMatchPageByRequest.remove(requestId)
            _uiState.update {
                it.copy(
                    requests = it.requests.filterNot { request -> request.id == requestId },
                    rideTitles = it.rideTitles - requestId,
                    pendingSyncRequestIds = it.pendingSyncRequestIds - requestId,
                    expandedRequestId = if (it.expandedRequestId == requestId) null else it.expandedRequestId,
                    errorMessage = null,
                    matchErrorMessage = null
                )
            }
        }
    }

    private suspend fun rideRequestRepositoryDeletePending(requestId: String) {
        repository.deletePendingRequest(requestId)
    }

    fun loadMoreMatches(context: Context, requestId: String) {
        val passengerId = supabase.auth.currentUserOrNull()?.id
        if (passengerId == null) {
            _uiState.update { it.copy(matchErrorMessage = "No logged in user found.") }
            return
        }

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
                val matchPage = loadMatchesPage(requestId, passengerId, page)
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
                val currentReservationCount = reservationRepository
                    .getReservationCountByOffer(offerId)
                    .getOrThrow("Could not check available spots.")

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
                            .getOrThrow("Could not delete ride request.")
                        nextMatchPageByRequest.remove(requestId)
                        _uiState.update {
                            it.copy(
                                bookingOfferId = null,
                                expandedRequestId = null,
                                requests = it.requests.filterNot { request -> request.id == requestId },
                                rideTitles = it.rideTitles - requestId,
                                matchedOffers = (it.matchedOffers - requestId)
                                    .mapValues { (_, offers) -> offers.filterNot { offer -> offer.id == offerId } },
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
        return reservationRepository
            .getReservationCountsByOffers(offers.map { it.id })
            .getOrThrow("Could not load reservation counts.")
    }

    private suspend fun loadRequestsPage(passengerId: String, after: String, page: Int): List<RideRequest> {
        val from = page * PAGE_SIZE.toLong()
        val to = from + PAGE_SIZE - 1

        return repository
            .getFutureRequestsByPassenger(passengerId, after, from, to)
            .getOrThrow("Could not load ride requests.")
            .map { it.toDomain() }
    }

    private fun mergePendingAndSyncedRequests(
        pendingRequests: List<RideRequest>,
        syncedRequests: List<RideRequest>
    ): List<RideRequest> {
        val pendingIds = pendingRequests.map { it.id }.toSet()
        return (pendingRequests + syncedRequests.filterNot { it.id in pendingIds })
            .sortedBy { it.desiredDepartureTimeMillis }
    }

    private suspend fun loadMatchesPage(requestId: String, passengerId: String, page: Int): MatchPage {
        val from = page * PAGE_SIZE.toLong()
        val to = from + PAGE_SIZE - 1
        val matches = rideMatchRepository
            .getMatchesByRequest(requestId, from, to)
            .getOrThrow("Could not load matches.")
        val offerIds = matches.map { it.rideOfferId }
        val offers = rideOfferRepository
            .getFutureOffersByIds(offerIds, System.currentTimeMillis().toTimestampz())
            .getOrThrow("Could not load matched offers.")
            .map { it.toDomain() }
        val bookedOfferIds = loadBookedOfferIds(passengerId, offerIds)

        return MatchPage(
            offers = offers.filterNot { it.id in bookedOfferIds },
            hasMore = matches.size == PAGE_SIZE
        )
    }

    private suspend fun loadBookedOfferIds(passengerId: String, offerIds: List<String>): Set<String> {
        return reservationRepository
            .getReservationsByOffers(offerIds)
            .getOrThrow("Could not load reservations.")
            .filter { it.passengerId == passengerId }
            .map { it.rideOfferId }
            .toSet()
    }
}

private data class MatchPage(
    val offers: List<RideOffer>,
    val hasMore: Boolean
)

private const val PAGE_SIZE = 10

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
