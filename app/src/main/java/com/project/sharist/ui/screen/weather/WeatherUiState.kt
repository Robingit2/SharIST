package com.project.sharist.ui.screen.weather
import com.project.sharist.data.model.weather.Weather
import com.project.sharist.data.model.weather.HourlyForecast
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: Weather? = null,
    val forecast: List<HourlyForecast> = emptyList(),
    val error: String? = null
)