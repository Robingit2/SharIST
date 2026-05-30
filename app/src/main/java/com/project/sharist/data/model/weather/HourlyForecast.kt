package com.project.sharist.data.model.weather

data class HourlyForecast(
    val time: String,
    val temperature: Int,
    val weatherCode: Int
)