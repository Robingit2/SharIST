package com.project.sharist.ui.screen.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.repository.cachedReservationRepository
import com.project.sharist.data.repository.cachedRideOfferRepository
import com.project.sharist.data.repository.cachedUserRepository
import com.project.sharist.domain.model.RideOffer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReservationsScreen(
    onDriverClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val reservationRepository = remember(context) { cachedReservationRepository(context) }
    val rideOfferRepository = remember(context) { cachedRideOfferRepository(context) }
    val userRepository = remember(context) { cachedUserRepository(context) }
    val viewModel: ReservationsViewModel = viewModel(
        factory = ReservationsViewModelFactory(
            reservationRepository = reservationRepository,
            rideOfferRepository = rideOfferRepository,
            userRepository = userRepository
        )
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadReservations(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reservations", style = MaterialTheme.typography.headlineLarge)

        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            state.errorMessage != null -> {
                Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.loadReservations(context) }) {
                    Text("Retry")
                }
            }

            state.reservations.isEmpty() -> {
                Text("No reservations yet.")
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.reservations, key = { it.id }) { offer ->
                        ReservationItem(
                            offer = offer,
                            title = state.rideTitles[offer.id],
                            driverName = state.driverNames[offer.driverId],
                            freeSpots = (offer.vehicleCapacity - (state.reservationCounts[offer.id] ?: 0)).coerceAtLeast(0),
                            onDriverClick = { onDriverClick(offer.driverId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationItem(
    offer: RideOffer,
    title: String?,
    driverName: String?,
    freeSpots: Int,
    onDriverClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title ?: "${offer.departure.latitude}, ${offer.departure.longitude} -> ${offer.arrival.latitude}, ${offer.arrival.longitude}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Departure: ${offer.departureTimeMillis.formatDateTime()}")
            Text("Arrival: ${offer.estimatedArrivalTimeMillis.formatDateTime()}")
            Text("Cost: ${offer.cost}")
            Text("Free spots: $freeSpots")
            Text("Recurring: ${offer.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")

            TextButton(onClick = onDriverClick) {
                Text("Driver: ${driverName ?: offer.driverId}")
            }
        }
    }
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}
