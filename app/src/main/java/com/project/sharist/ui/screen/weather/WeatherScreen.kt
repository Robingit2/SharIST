package com.project.sharist.ui.screen.weather

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {

    val state by viewModel.state.collectAsState()

    Column {

        when {

            state.isLoading -> {
                Text("Loading...")
            }

            state.error != null -> {
                Text("Error: ${state.error}")
            }

            else -> {
                state.weather?.let { weather ->
                    Text("Temp: ${weather.temp}°C")
                    Text("Wind: ${weather.wind} km/h")
                    Text(weather.description)
                }
            }
        }
    }
}