package com.project.sharist.api
import retrofit2.http.GET
import retrofit2.http.Query
import com.project.sharist.api.dto.WeatherResponse

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String =
            "temperature_2m,weather_code,wind_speed_10m",
        @Query("hourly") hourly: String =
            "temperature_2m,weather_code"
    ): WeatherResponse
}