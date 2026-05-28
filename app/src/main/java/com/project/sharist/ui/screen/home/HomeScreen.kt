package com.project.sharist.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment

import com.project.sharist.data.model.user.RoleType
import com.project.sharist.ui.screen.map.OpenStreetMapView
import com.project.sharist.ui.screen.ride_offer.RideOfferScreen
import com.project.sharist.ui.screen.ride_request.RideRequestScreen

@Composable
fun HomeScreen(
    role: RoleType,
    viewModel: HomeViewModel = viewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OpenStreetMapView()

        if (role == RoleType.PASSENGER) {
            RideRequestScreen(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        if (role == RoleType.DRIVER) {
            RideOfferScreen(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}
