package com.project.sharist.data.repository

import android.util.Log
import com.project.sharist.api.RetrofitClient
import com.project.sharist.data.mapper.mapForecast
import com.project.sharist.data.mapper.mapWeather
import com.project.sharist.data.model.weather.WeatherResult

class WeatherRepository {

    suspend fun getWeather(lat: Double, lon: Double): WeatherResult {

        val response = RetrofitClient.api.getWeather(lat, lon)

        Log.d("WEATHER_RAW", response.toString())

        return WeatherResult(
            weather = mapWeather(response),
            forecast = mapForecast(response)
        )
    }
}