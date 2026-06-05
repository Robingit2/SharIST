package com.project.sharist.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.project.sharist.data.model.user.RoleType
import com.project.sharist.ui.screen.map.OpenStreetMapView
import com.project.sharist.ui.screen.weather.WeatherViewModel

import com.project.sharist.ui.screen.favorite.FavoriteViewModel

@Composable
fun HomeScreen(
    role: RoleType,
    onCreateRideOfferClick: () -> Unit,
    onCreateRideRequestClick: () -> Unit,
    weatherViewModel: WeatherViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel,
) {
    Box(Modifier.fillMaxSize()) {

        OpenStreetMapView(
            weatherViewModel = weatherViewModel,
            favoriteViewModel = favoriteViewModel
        )

        if (role == RoleType.PASSENGER) {
            Button(
                onClick = onCreateRideRequestClick,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Create ride request")
            }
        }

        if (role == RoleType.DRIVER) {
            Button(
                onClick = onCreateRideOfferClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Create ride offer")
            }
        }
    }
}
