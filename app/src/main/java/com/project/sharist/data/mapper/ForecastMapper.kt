package com.project.sharist.data.mapper

import com.project.sharist.api.dto.WeatherResponse
import com.project.sharist.data.model.weather.HourlyForecast

fun mapForecast(dto: WeatherResponse): List<HourlyForecast> {

    val times = dto.hourly.time
    val temps = dto.hourly.temperature
    val codes = dto.hourly.weatherCode

    return times.mapIndexed { index, time ->
        HourlyForecast(
            time = time.takeLast(5),
            temperature = temps[index].toInt(),
            weatherCode = codes[index]
        )
    }
}