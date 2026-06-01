package com.project.sharist.ui.screen.available_rides

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.domain.model.RecurringType
import com.project.sharist.domain.model.RideOffer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AvailableRidesScreen(
    onDriverClick: (String) -> Unit,
    viewModel: AvailableRidesViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Available rides", style = MaterialTheme.typography.headlineLarge)

        AddressFilterSection(
            title = "Departure",
            address = state.departureAddress,
            latitude = state.departureLat,
            longitude = state.departureLng,
            radiusMeters = state.departureRadiusMeters,
            enabled = !state.isLoading,
            onAddressChange = viewModel::updateDepartureAddress,
            onLatitudeChange = viewModel::updateDepartureLat,
            onLongitudeChange = viewModel::updateDepartureLng,
            onRadiusChange = viewModel::updateDepartureRadiusMeters,
            onSearchClick = { viewModel.searchDepartureAddress(context) }
        )

        AddressFilterSection(
            title = "Arrival",
            address = state.arrivalAddress,
            latitude = state.arrivalLat,
            longitude = state.arrivalLng,
            radiusMeters = state.arrivalRadiusMeters,
            enabled = !state.isLoading,
            onAddressChange = viewModel::updateArrivalAddress,
            onLatitudeChange = viewModel::updateArrivalLat,
            onLongitudeChange = viewModel::updateArrivalLng,
            onRadiusChange = viewModel::updateArrivalRadiusMeters,
            onSearchClick = { viewModel.searchArrivalAddress(context) }
        )

        DateTimePickerRow(
            label = "Desired departure time",
            selectedMillis = state.desiredDepartureTimeMillis,
            enabled = !state.isLoading,
            onSelected = viewModel::updateDesiredDepartureTimeMillis
        )

        OutlinedTextField(
            value = state.departureToleranceMinutes,
            onValueChange = viewModel::updateDepartureToleranceMinutes,
            label = { Text("Departure tolerance minutes") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )

        Text("Recurring", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecurringType.entries.forEach { recurringType ->
                OutlinedButton(
                    onClick = { viewModel.updateRecurringType(recurringType) },
                    enabled = !state.isLoading
                ) {
                    Text(
                        if (state.recurringType == recurringType) {
                            "${recurringType.name.lowercase().replaceFirstChar { it.titlecase() }} *"
                        } else {
                            recurringType.name.lowercase().replaceFirstChar { it.titlecase() }
                        }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.searchAvailableRides(context) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text("Search rides")
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Text("Results", style = MaterialTheme.typography.titleMedium)

        if (!state.isLoading && state.results.isEmpty()) {
            Text("No rides found.")
        } else {
            LazyColumn(
                modifier = Modifier.height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.results, key = { it.id }) { offer ->
                    RideOfferResultItem(
                        offer = offer,
                        title = state.rideTitles[offer.id],
                        driverName = state.driverNames[offer.driverId],
                        onDriverClick = onDriverClick,
                        isBooking = state.isBooking,
                        onBookClick = { viewModel.bookRide(offer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressFilterSection(
    title: String,
    address: String,
    latitude: String,
    longitude: String,
    radiusMeters: String,
    enabled: Boolean,
    onAddressChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("$title address") },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            Button(
                onClick = onSearchClick,
                enabled = enabled
            ) {
                Text("Search")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = latitude,
                onValueChange = onLatitudeChange,
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = onLongitudeChange,
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
        }

        OutlinedTextField(
            value = radiusMeters,
            onValueChange = onRadiusChange,
            label = { Text("$title radius meters") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}

@Composable
private fun DateTimePickerRow(
    label: String,
    selectedMillis: Long?,
    enabled: Boolean,
    onSelected: (Long) -> Unit
) {
    val context = LocalContext.current

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = selectedMillis?.formatDateTime().orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        Button(
            onClick = {
                showDateTimePicker(
                    context = context,
                    initialMillis = selectedMillis,
                    onSelected = onSelected
                )
            },
            enabled = enabled
        ) {
            Text("Pick")
        }
    }
}

@Composable
private fun RideOfferResultItem(
    offer: RideOffer,
    title: String?,
    driverName: String?,
    onDriverClick: (String) -> Unit,
    isBooking: Boolean,
    onBookClick: () -> Unit
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
            Text("Capacity: ${offer.vehicleCapacity}")
            Text("Recurring: ${offer.recurringType.name.lowercase().replaceFirstChar { it.titlecase() }}")

            TextButton(onClick = { onDriverClick(offer.driverId) }) {
                Text("Driver: ${driverName ?: offer.driverId}")
            }

            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBooking
            ) {
                Text("Book ride")
            }
        }
    }
}

private fun showDateTimePicker(
    context: Context,
    initialMillis: Long?,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialMillis ?: System.currentTimeMillis()
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}
