package com.project.sharist.ui.screen.ride_request

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.repository.RideRequestInsertResult
import com.project.sharist.data.repository.cachedRideRequestRepository
import com.project.sharist.data.usecase.ride.InsertRideRequestUseCase
import com.project.sharist.domain.model.RecurringType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RideRequestScreen(
    onRideRequestSaved: () -> Unit = {},
    pendingRequestId: String? = null,
    viewModel: RideRequestViewModel = rideRequestViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(pendingRequestId) {
        pendingRequestId?.let(viewModel::loadPendingRequest)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            val message = when (state.saveResult) {
                RideRequestInsertResult.PendingSync -> {
                    if (state.editingPendingRequestId != null) {
                        "Ride request changes saved."
                    } else {
                        "Ride request saved offline."
                    }
                }
                RideRequestInsertResult.Synced,
                null -> "Ride request created."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onRideRequestSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (pendingRequestId == null) "Create ride request" else "Edit ride request",
            style = MaterialTheme.typography.headlineLarge
        )

        AddressSection(
            title = "Departure",
            address = state.departureAddress,
            latitude = state.departureLat,
            longitude = state.departureLng,
            radiusMeters = state.departureRadiusMeters,
            enabled = !state.isLoading,
            onAddressChange = viewModel::updateDepartureAddress,
            onLatitudeChange = viewModel::updateDepartureLat,
            onLongitudeChange = viewModel::updateDepartureLng,
            onRadiusMetersChange = viewModel::updateDepartureRadiusMeters,
            onSearchClick = { viewModel.searchDepartureAddress(context) }
        )

        AddressSection(
            title = "Arrival",
            address = state.arrivalAddress,
            latitude = state.arrivalLat,
            longitude = state.arrivalLng,
            radiusMeters = state.arrivalRadiusMeters,
            enabled = !state.isLoading,
            onAddressChange = viewModel::updateArrivalAddress,
            onLatitudeChange = viewModel::updateArrivalLat,
            onLongitudeChange = viewModel::updateArrivalLng,
            onRadiusMetersChange = viewModel::updateArrivalRadiusMeters,
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

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(if (pendingRequestId == null) "Create request" else "Save changes")
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
    radiusMeters: String,
    enabled: Boolean,
    onAddressChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onRadiusMetersChange: (String) -> Unit,
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
            onValueChange = onRadiusMetersChange,
            label = { Text("$title tolerance radius meters") },
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

private fun showDateTimePicker(
    context: Context,
    initialMillis: Long?,
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

    datePickerDialog.show()
}

private fun Long.formatDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(this)
}

@Composable
private fun rideRequestViewModel(): RideRequestViewModel {
    val context = LocalContext.current
    val rideRequestRepository = remember(context) { cachedRideRequestRepository(context) }
    val insertRideRequestUseCase = remember(context) {
        InsertRideRequestUseCase(
            rideRequestRepository
        )
    }

    return viewModel(
        factory = RideRequestViewModelFactory(insertRideRequestUseCase, rideRequestRepository)
    )
}
