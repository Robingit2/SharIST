package com.project.sharist.ui.screen.ride_request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.domain.model.RideRequest
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyRideRequestsScreen(
    onDriverClick: (String) -> Unit,
    viewModel: MyRideRequestsViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRequests(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My ride requests", style = MaterialTheme.typography.headlineLarge)

        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            state.errorMessage != null -> {
                Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { viewModel.loadRequests(context) }) {
                    Text("Retry")
                }
            }

            state.requests.isEmpty() -> {
                Text("No ride requests created.")
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.requests, key = { it.id }) { request ->
                        RideRequestItem(
                            request = request,
                            title = state.rideTitles[request.id],
                            isExpanded = state.expandedRequestId == request.id,
                            isLoadingMatches = state.loadingMatchesRequestId == request.id,
                            matchErrorMessage = state.matchErrorMessage,
                            matches = state.matchedOffers[request.id].orEmpty(),
                            offerTitles = state.matchedOfferTitles,
                            driverNames = state.driverNames,
                            reservationCounts = state.reservationCounts,
                            bookingOfferId = state.bookingOfferId,
                            onShowMatchesClick = { viewModel.toggleMatches(context, request.id) },
                            onDriverClick = onDriverClick,
                            onBookClick = { offerId -> viewModel.bookMatchedRide(request.id, offerId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RideRequestItem(
    request: RideRequest,
    title: String?,
    isExpanded: Boolean,
    isLoadingMatches: Boolean,
    matchErrorMessage: String?,
    matches: List<RideOffer>,
    offerTitles: Map<String, String>,
    driverNames: Map<String, String>,
    reservationCounts: Map<String, Int>,
    bookingOfferId: String?,
    onShowMatchesClick: () -> Unit,
    onDriverClick: (String) -> Unit,
    onBookClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title ?: "${request.departure.latitude}, ${request.departure.longitude} -> ${request.arrival.latitude}, ${request.arrival.longitude}",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Desired departure: ${request.desiredDepartureTimeMillis.formatDateTime()}")
            Text("Departure radius: ${request.departureRadiusMeters} m")
            Text("Arrival radius: ${request.arrivalRadiusMeters} m")
            Text("Departure tolerance: ${request.departureToleranceMinutes} minutes")
            Text("Recurring: ${request.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onShowMatchesClick) {
                    Text(if (isExpanded) "Hide matches" else "Show matches")
                }
            }

            if (isExpanded) {
                MatchesSection(
                    isLoading = isLoadingMatches,
                    errorMessage = matchErrorMessage,
                    matches = matches,
                    offerTitles = offerTitles,
                    driverNames = driverNames,
                    reservationCounts = reservationCounts,
                    bookingOfferId = bookingOfferId,
                    onDriverClick = onDriverClick,
                    onBookClick = onBookClick
                )
            }
        }
    }
}

@Composable
private fun MatchesSection(
    isLoading: Boolean,
    errorMessage: String?,
    matches: List<RideOffer>,
    offerTitles: Map<String, String>,
    driverNames: Map<String, String>,
    reservationCounts: Map<String, Int>,
    bookingOfferId: String?,
    onDriverClick: (String) -> Unit,
    onBookClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Matches", style = MaterialTheme.typography.titleMedium)

        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(errorMessage, color = MaterialTheme.colorScheme.error)
            matches.isEmpty() -> Text("No matches found yet.")
            else -> matches.forEach { offer ->
                MatchedOfferItem(
                    offer = offer,
                    title = offerTitles[offer.id],
                    driverName = driverNames[offer.driverId],
                    freeSpots = (offer.vehicleCapacity - (reservationCounts[offer.id] ?: 0)).coerceAtLeast(0),
                    isBooking = bookingOfferId == offer.id,
                    onDriverClick = { onDriverClick(offer.driverId) },
                    onBookClick = { onBookClick(offer.id) }
                )
            }
        }
    }
}

@Composable
private fun MatchedOfferItem(
    offer: RideOffer,
    title: String?,
    driverName: String?,
    freeSpots: Int,
    isBooking: Boolean,
    onDriverClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title ?: "${offer.departure.latitude}, ${offer.departure.longitude} -> ${offer.arrival.latitude}, ${offer.arrival.longitude}",
                style = MaterialTheme.typography.titleSmall
            )
            Text("Departure: ${offer.departureTimeMillis.formatDateTime()}")
            Text("Arrival: ${offer.estimatedArrivalTimeMillis.formatDateTime()}")
            Text("Cost: ${offer.cost}")
            Text("Free spots: $freeSpots")
            Text("Recurring: ${offer.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")

            OutlinedButton(onClick = onDriverClick) {
                Text("Driver: ${driverName ?: offer.driverId}")
            }

            OutlinedButton(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBooking && freeSpots > 0
            ) {
                Text(if (freeSpots > 0) "Book ride" else "Fully booked")
            }
        }
    }
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}
