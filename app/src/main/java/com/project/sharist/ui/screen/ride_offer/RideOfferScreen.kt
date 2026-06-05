package com.project.sharist.ui.screen.ride_offer

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.repository.cachedRideOfferRepository
import com.project.sharist.data.usecase.ride.InsertRideOfferUseCase
import com.project.sharist.domain.model.RecurringType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RideOfferScreen(
    onRideOfferSaved: () -> Unit,
    viewModel: RideOfferViewModel = rideOfferViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            Toast.makeText(context, "Ride offer created.", Toast.LENGTH_SHORT).show()
            onRideOfferSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create ride offer", style = MaterialTheme.typography.headlineLarge)

        AddressSection(
            title = "Departure",
            address = state.departureAddress,
            latitude = state.departureLat,
            longitude = state.departureLng,
            enabled = !state.isLoading,
            onAddressChange = viewModel::updateDepartureAddress,
            onLatitudeChange = viewModel::updateDepartureLat,
            onLongitudeChange = viewModel::updateDepartureLng,
            onSearchClick = { viewModel.searchDepartureAddress(context) }
        )

        AddressSection(
            title = "Arrival",
            address = state.arrivalAddress,
            latitude = state.arrivalLat,
            longitude = state.arrivalLng,
            enabled = !state.isLoading,
            onAddressChange = viewModel::updateArrivalAddress,
            onLatitudeChange = viewModel::updateArrivalLat,
            onLongitudeChange = viewModel::updateArrivalLng,
            onSearchClick = { viewModel.searchArrivalAddress(context) }
        )

        DateTimePickerRow(
            label = "Departure time",
            selectedMillis = state.departureTimeMillis,
            enabled = !state.isLoading,
            onSelected = viewModel::updateDepartureTimeMillis
        )

        DateTimePickerRow(
            label = "Estimated arrival",
            selectedMillis = state.estimatedArrivalTimeMillis,
            minMillis = state.departureTimeMillis,
            enabled = !state.isLoading,
            onSelected = viewModel::updateEstimatedArrivalTimeMillis
        )

        OutlinedTextField(
            value = state.cost,
            onValueChange = viewModel::updateCost,
            label = { Text("Cost (€)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )

        OutlinedTextField(
            value = state.vehicleCapacity,
            onValueChange = viewModel::updateVehicleCapacity,
            label = { Text("Vehicle capacity") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )

        OutlinedTextField(
            value = state.cancellationWindowMinutes,
            onValueChange = viewModel::updateCancellationWindowMinutes,
            label = { Text("Cancellation window minutes") },
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

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Publish ride")
            }
        }
    }
}

@Composable
private fun DateTimePickerRow(
    label: String,
    selectedMillis: Long?,
    minMillis: Long? = null,
    enabled: Boolean,
    onSelected: (Long) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)

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
                        minMillis = minMillis,
                        onSelected = onSelected
                    )
                },
                enabled = enabled
            ) {
                Text("Pick")
            }
        }
    }
}

@Composable
private fun AddressSection(
    title: String,
    address: String,
    latitude: String,
    longitude: String,
    enabled: Boolean,
    onAddressChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
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
    }
}

private fun showDateTimePicker(
    context: Context,
    initialMillis: Long?,
    minMillis: Long? = null,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialMillis ?: System.currentTimeMillis()
    }

    val datePickerDialog = DatePickerDialog(
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
                    if (minMillis != null && calendar.timeInMillis <= minMillis) {
                        Toast.makeText(
                            context,
                            "Arrival must be after departure.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@TimePickerDialog
                    }
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
    )

    minMillis?.let {
        datePickerDialog.datePicker.minDate = it
    }

    datePickerDialog.show()
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}

@Composable
private fun rideOfferViewModel(): RideOfferViewModel {
    val context = LocalContext.current
    val insertRideOfferUseCase = remember(context) {
        InsertRideOfferUseCase(
            cachedRideOfferRepository(context)
        )
    }

    return viewModel(
        factory = RideOfferViewModelFactory(insertRideOfferUseCase)
    )
}
