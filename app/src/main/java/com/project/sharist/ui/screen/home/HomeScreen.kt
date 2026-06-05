package com.project.sharist.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.project.sharist.data.model.user.RoleType
import com.project.sharist.ui.screen.favorite.FavoriteDisplayMode
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
    var centerOnUserLocationTrigger by remember { mutableIntStateOf(0) }
    val favoriteDisplayMode by favoriteViewModel.displayMode.collectAsState()
    val showFavoriteMarkers = favoriteDisplayMode == FavoriteDisplayMode.ALL

    Box(Modifier.fillMaxSize()) {

        OpenStreetMapView(
            weatherViewModel = weatherViewModel,
            favoriteViewModel = favoriteViewModel,
            centerOnUserLocationTrigger = centerOnUserLocationTrigger
        )

        FloatingActionButton(
            onClick = { centerOnUserLocationTrigger += 1 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Center map on my location"
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 88.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Favorites")
                Switch(
                    checked = showFavoriteMarkers,
                    onCheckedChange = favoriteViewModel::setShowAllFavorites
                )
            }
        }

        if (role == RoleType.PASSENGER) {
            Button(
                onClick = {
                    favoriteViewModel.clearFavoritesFromMap()
                    onCreateRideRequestClick()
                },
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
                onClick = {
                    favoriteViewModel.clearFavoritesFromMap()
                    onCreateRideOfferClick()
                },
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
