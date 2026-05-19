package com.project.sharist.ui.screen.ride_offer

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier

@Composable
fun RideOfferScreen(
    modifier: Modifier = Modifier,
) {
    val vm: RideOfferViewModel = viewModel()
    val state by vm.state.collectAsState()

    RideOfferCard(
        state = state,
        onPickupChange = vm::updatePickup,
        onDestinationChange = vm::updateDestination,
        onCostChange = vm::updateCost,
        onPublishClick = vm::save,
        modifier = modifier
    )
}