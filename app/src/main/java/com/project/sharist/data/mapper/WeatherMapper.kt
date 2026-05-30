package com.project.sharist.data.mapper
import com.project.sharist.data.model.weather.Weather
import com.project.sharist.api.dto.WeatherResponse


fun mapWeather(dto: WeatherResponse): Weather {
    return Weather(
        temp = dto.current.temperature,
        wind = dto.current.windSpeed,
        description = weatherCodeToText(dto.current.weatherCode)
    )
}

fun weatherCodeToText(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2 -> "Partly cloudy"
        3 -> "Cloudy"
        61, 63, 65 -> "Rain"
        else -> "Unknown"
    }
}
