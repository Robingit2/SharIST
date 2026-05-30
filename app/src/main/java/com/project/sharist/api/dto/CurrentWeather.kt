package com.project.sharist.api.dto

import com.google.gson.annotations.SerializedName

data class CurrentWeather(

    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double,

    @SerializedName("weather_code")
    val weatherCode: Int
)
