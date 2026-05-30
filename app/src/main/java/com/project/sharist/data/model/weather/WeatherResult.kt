package com.project.sharist.data.model.weather

data class WeatherResult(
    val weather: Weather,
    val forecast: List<HourlyForecast>
)