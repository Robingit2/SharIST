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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.domain.model.RideOffer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyRideOffersScreen(
    viewModel: MyRideOffersViewModel = myRideOffersViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOffers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("My ride offers", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.errorMessage != null) {
            Text(uiState.errorMessage.orEmpty())
        } else if (uiState.offers.isEmpty()) {
            Text("No ride offers created.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.offers, key = { it.id }) { offer ->
                    RideOfferItem(
                        offer = offer,
                        onDeleteClick = { viewModel.deleteOffer(offer) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RideOfferItem(
    offer: RideOffer,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${offer.departure.latitude}, ${offer.departure.longitude} -> ${offer.arrival.latitude}, ${offer.arrival.longitude}",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Departure: ${offer.departureTimeMillis.formatDateTime()}")
            Text("Arrival: ${offer.estimatedArrivalTimeMillis.formatDateTime()}")
            Text("Cost: ${offer.cost}")
            Text("Capacity: ${offer.vehicleCapacity}")
            Text("Cancellation: ${offer.cancellationWindowMinutes} minutes")
            Text("Recurring: ${offer.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDeleteClick) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun myRideOffersViewModel(): MyRideOffersViewModel {
    val repository = remember {
        RideOfferRepository()
    }

    return viewModel(
        factory = MyRideOffersViewModelFactory(repository)
    )
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}
