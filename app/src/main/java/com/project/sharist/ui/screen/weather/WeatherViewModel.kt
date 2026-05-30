package com.project.sharist.ui.screen.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
class WeatherViewModel : ViewModel() {

    private val repo = WeatherRepository()

    private val _state = MutableStateFlow(WeatherUiState())
    val state = _state.asStateFlow()


    fun loadWeather(lat: Double, lon: Double) {

        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            try {
                val result = repo.getWeather(lat, lon)

                _state.value = WeatherUiState(
                    isLoading = false,
                    weather = result.weather,
                    forecast = result.forecast
                )

                Log.d("WEATHER_VM", "forecast size = ${result.forecast.size}")

            } catch (e: Exception) {

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}