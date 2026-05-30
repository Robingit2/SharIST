package com.project.sharist.ui.screen.weather

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


import androidx.compose.foundation.layout.Spacer
import com.project.sharist.data.model.weather.Weather
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import com.project.sharist.data.model.weather.HourlyForecast
import com.project.sharist.data.mapper.weatherIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically


import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable


/*@Composable
fun WeatherOverlay(
    state: WeatherUiState
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            when {

                state.isLoading -> {
                    Text("Loading weather...")
                }

                state.error != null -> {
                    Text("Error: ${state.error}")
                }

                state.weather != null -> {

                    Text(
                        text = "Temperature: ${state.weather.temp}°C"
                    )

                    Text(
                        text = "Wind: ${state.weather.wind} km/h"
                    )

                    Text(
                        text = state.weather.description
                    )
                }
            }
        }
    }
}*/
@Composable
fun ForecastItem(
    time: String,
    temp: Int,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(time)

        Spacer(modifier = Modifier.height(4.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text("$temp°")
    }
}
@Composable
fun HourlyForecastRow(
    forecast: List<HourlyForecast>
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(forecast) { item ->
            ForecastItem(
                time = item.time,
                temp = item.temperature,
                icon = weatherIcon(item.weatherCode)
            )
        }
    }
}
@Composable
fun WeatherOverlay(
    weather: Weather?,
    forecast: List<HourlyForecast>
) {
    if (weather == null) return

    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            // HEADER (always visible)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${weather.temp}°C",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = weather.description,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // EXPANDABLE FORECAST
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {

                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (forecast.isNotEmpty()) {
                        HourlyForecastRow(forecast)
                    } else {
                        Text("Loading forecast...")
                    }
                }
            }
        }
    }
}