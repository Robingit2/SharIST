package com.project.sharist.ui.screen.history

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
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.cachedUserRepository
import com.project.sharist.domain.model.RideOffer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    role: RoleType,
    onUserClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val userRepository = remember(context) { cachedUserRepository(context) }
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(userRepository)
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(role) {
        viewModel.loadHistory(context, role)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("${role.name.lowercase().replaceFirstChar { it.titlecase() }} history", style = MaterialTheme.typography.headlineLarge)

        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            state.errorMessage != null -> {
                Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { viewModel.loadHistory(context, role) }) {
                    Text("Retry")
                }
            }

            state.offers.isEmpty() -> {
                Text("No past rides found.")
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.offers, key = { it.id }) { offer ->
                        HistoryRideItem(
                            role = role,
                            offer = offer,
                            title = state.rideTitles[offer.id],
                            driverName = state.driverNames[offer.driverId],
                            passengerIds = state.passengerIdsByOffer[offer.id].orEmpty(),
                            passengerNames = state.passengerNames,
                            onUserClick = onUserClick
                        )
                    }

                    if (state.hasMoreOffers) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (state.isLoadingMore) {
                                    Text("Loading more...")
                                } else {
                                    OutlinedButton(onClick = { viewModel.loadMoreHistory(context, role) }) {
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
}

@Composable
private fun HistoryRideItem(
    role: RoleType,
    offer: RideOffer,
    title: String?,
    driverName: String?,
    passengerIds: List<String>,
    passengerNames: Map<String, String>,
    onUserClick: (String) -> Unit
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
            Text("Recurring: ${offer.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")

            if (role == RoleType.PASSENGER) {
                TextButton(onClick = { onUserClick(offer.driverId) }) {
                    Text("Driver: ${driverName ?: offer.driverId}")
                }
            } else {
                Text("Passengers", style = MaterialTheme.typography.titleSmall)
                if (passengerIds.isEmpty()) {
                    Text("No passengers.")
                } else {
                    passengerIds.forEach { passengerId ->
                        TextButton(onClick = { onUserClick(passengerId) }) {
                            Text(passengerNames[passengerId] ?: passengerId)
                        }
                    }
                }
            }
        }
    }
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}
