package com.project.sharist.ui.screen.ride_offer


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RideOfferCard(
    state: RideOfferUiState,
    onPickupChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {

            Text("Create Ride Offer")
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.pickup,
                onValueChange = onPickupChange,
                label = { Text("Pickup") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.destination,
                onValueChange = onDestinationChange,
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.cost,
                onValueChange = onCostChange,
                label = { Text("Cost") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onPublishClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publish Ride")
            }
        }
    }
}