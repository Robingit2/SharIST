package com.project.sharist.ui.screen.ride_request

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.sharist.domain.model.RecurringType

@Composable
fun RideRequestForm(
    state: RideRequestUiState,
    onDepartureChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onPassengersChange: (Int) -> Unit,
    onRecurringChange: (RecurringType) -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Find a Ride",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.departure,
                onValueChange = onDepartureChange,
                label = { Text("Departure") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.destination,
                onValueChange = onDestinationChange,
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.passengers.toString(),
                onValueChange = {
                    onPassengersChange(it.toIntOrNull() ?: 1)
                },
                label = { Text("Passengers") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row {

                FilterChip(
                    selected = state.recurringType == RecurringType.NONE,
                    onClick = { onRecurringChange(RecurringType.NONE) },
                    label = { Text("None") }
                )

                Spacer(Modifier.width(8.dp))

                FilterChip(
                    selected = state.recurringType == RecurringType.DAILY,
                    onClick = { onRecurringChange(RecurringType.DAILY) },
                    label = { Text("Daily") }
                )

                Spacer(Modifier.width(8.dp))

                FilterChip(
                    selected = state.recurringType == RecurringType.WEEKLY,
                    onClick = { onRecurringChange(RecurringType.WEEKLY) },
                    label = { Text("Weekly") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* search rides */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Ride")
            }
        }
    }
}