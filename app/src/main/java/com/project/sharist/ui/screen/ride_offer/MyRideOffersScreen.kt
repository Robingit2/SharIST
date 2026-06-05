package com.project.sharist.ui.screen.ride_offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.repository.cachedRideOfferRepository
import com.project.sharist.data.repository.cachedUserRepository
import com.project.sharist.domain.model.RideOffer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyRideOffersScreen(
    onPassengerClick: (String) -> Unit,
    onEditPendingOfferClick: (String) -> Unit,
    viewModel: MyRideOffersViewModel = myRideOffersViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOffers(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("My ride offers", style = MaterialTheme.typography.headlineLarge)
        if (uiState.errorMessage != null && uiState.offers.isNotEmpty()) {
            Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.errorMessage != null && uiState.offers.isEmpty()) {
            Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
        } else if (uiState.offers.isEmpty()) {
            Text("No ride offers created.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.offers, key = { it.id }) { offer ->
                    RideOfferItem(
                        offer = offer,
                        title = uiState.rideTitles[offer.id],
                        freeSpots = uiState.reservationCounts[offer.id]
                            ?.let { reservationCount -> (offer.vehicleCapacity - reservationCount).coerceAtLeast(0) },
                        passengerIds = uiState.passengerIdsByOffer[offer.id].orEmpty(),
                        passengerNames = uiState.passengerNames,
                        isPassengerDataUnavailable = offer.id in uiState.unavailablePassengerOfferIds,
                        isPendingSync = offer.id in uiState.pendingSyncOfferIds,
                        isSyncing = uiState.syncingOfferId == offer.id,
                        onPassengerClick = onPassengerClick,
                        onDeleteClick = { viewModel.deleteOffer(offer) },
                        onEditPendingClick = { onEditPendingOfferClick(offer.id) },
                        onDeletePendingClick = { viewModel.deletePendingOffer(offer.id) },
                        onSyncPendingClick = { viewModel.syncPendingOffer(context, offer.id) }
                    )
                }

                if (uiState.hasMoreOffers) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (uiState.isLoadingMore) {
                                Text("Loading more...")
                            } else {
                                OutlinedButton(onClick = { viewModel.loadMoreOffers(context) }) {
                                    Text("Load more")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RideOfferItem(
    offer: RideOffer,
    title: String?,
    freeSpots: Int?,
    passengerIds: List<String>,
    passengerNames: Map<String, String>,
    isPassengerDataUnavailable: Boolean,
    isPendingSync: Boolean,
    isSyncing: Boolean,
    onPassengerClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onEditPendingClick: () -> Unit,
    onDeletePendingClick: () -> Unit,
    onSyncPendingClick: () -> Unit
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
            Text("Free spots: ${freeSpots?.toString() ?: "Unavailable"}")
            Text("Cancellation: ${offer.cancellationWindowMinutes} minutes")
            Text("Recurring: ${offer.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")
            if (isPendingSync) {
                Text("Pending sync", color = MaterialTheme.colorScheme.primary)
            }

            if (!isPendingSync) {
                Text("Passengers", style = MaterialTheme.typography.titleSmall)
                if (isPassengerDataUnavailable) {
                    Text("Passenger data unavailable.")
                } else if (passengerIds.isEmpty()) {
                    Text("No passengers yet.")
                } else {
                    passengerIds.forEach { passengerId ->
                        OutlinedButton(onClick = { onPassengerClick(passengerId) }) {
                            Text(passengerNames[passengerId] ?: passengerId)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isPendingSync) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onEditPendingClick,
                            enabled = !isSyncing
                        ) {
                            Text("Edit")
                        }
                        OutlinedButton(
                            onClick = onDeletePendingClick,
                            enabled = !isSyncing
                        ) {
                            Text("Delete")
                        }
                        OutlinedButton(
                            onClick = onSyncPendingClick,
                            enabled = !isSyncing
                        ) {
                            Text(if (isSyncing) "Sending..." else "Send")
                        }
                    }
                } else {
                    OutlinedButton(onClick = onDeleteClick) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun myRideOffersViewModel(): MyRideOffersViewModel {
    val context = LocalContext.current
    val repository = remember(context) {
        cachedRideOfferRepository(context)
    }
    val userRepository = remember(context) {
        cachedUserRepository(context)
    }

    return viewModel(
        factory = MyRideOffersViewModelFactory(
            repository = repository,
            userRepository = userRepository
        )
    )
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}
