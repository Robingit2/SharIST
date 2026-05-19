package com.project.sharist.ui.screen.ride_request

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RideRequestScreen(
    modifier: Modifier = Modifier
) {

    val vm: RideRequestViewModel = viewModel()
    val state by vm.state.collectAsState()

    RideRequestForm(
        state = state,
        onDepartureChange = vm::updateDeparture,
        onDestinationChange = vm::updateDestination,
        onPassengersChange = vm::updatePassengers,
        onRecurringChange = vm::updateRecurring,
        modifier = modifier
    )
}