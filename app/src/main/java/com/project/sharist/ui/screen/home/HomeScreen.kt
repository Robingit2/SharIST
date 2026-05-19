package com.project.sharist.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment

import com.project.sharist.ui.screen.map.OpenStreetMapView
import com.project.sharist.ui.screen.ride_offer.RideOfferScreen
import com.project.sharist.ui.screen.ride_request.RideRequestScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    role: String,
    viewModel: HomeViewModel = viewModel()
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {}
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {}
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            OpenStreetMapView()

            FloatingActionButton(
                onClick = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null)
            }

            if (role == "RIDER") {
                RideRequestScreen(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }


            if (role == "DRIVER") {
                RideOfferScreen(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}