package com.project.sharist.api.dto
import com.google.gson.annotations.SerializedName

data class HourlyWeather(
    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperature: List<Double>,

    @SerializedName("weather_code")
    val weatherCode: List<Int>
)
