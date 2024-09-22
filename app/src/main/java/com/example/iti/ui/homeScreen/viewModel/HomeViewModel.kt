package com.example.iti.ui.homeScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.model.DailyForecastElement
import com.example.iti.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class HomeViewModel(private val repository: RepositoryImpl) : ViewModel() {

    private val _weatherDataStateFlow = MutableStateFlow<ApiState>(ApiState.Loading)
    val weatherDataStateFlow: StateFlow<ApiState> get() = _weatherDataStateFlow

    private val _hourlyForecastDataStateFlow = MutableStateFlow<ApiState>(ApiState.Loading)
    val hourlyForecastDataStateFlow: StateFlow<ApiState> get() = _hourlyForecastDataStateFlow

    private val _dailyForecastDataStateFlow = MutableStateFlow<ApiState>(ApiState.Loading)
    val dailyForecastDataStateFlow: StateFlow<ApiState> get() = _dailyForecastDataStateFlow


    fun fetchCurrentWeatherDataByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherDataStateFlow.value = ApiState.Loading
            try {
                repository.fetchCurrentWeather(lat, lon).collect { weather ->
                    _weatherDataStateFlow.value = ApiState.Success(weather)
                }
            } catch (e: Exception) {
                _weatherDataStateFlow.value = ApiState.Failure(e.message ?: "Unknown Error")
            }
        }
    }

    fun fetchHourlyWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _hourlyForecastDataStateFlow.value = ApiState.Loading
            try {
                repository.fetchHourlyForecast(lat, lon).collect { hourly ->
                    _hourlyForecastDataStateFlow.value = ApiState.Success(hourly)
                }
            } catch (e: Exception) {
                _hourlyForecastDataStateFlow.value = ApiState.Failure(e.message ?: "Unknown Error")
            }
        }
    }

    fun fetchDailyWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherDataStateFlow.value = ApiState.Loading
            try {
                repository.fetchDailyForecast(lat, lon).collect { dailyForecast ->
                    val processedForecast = processForecastData(dailyForecast.list)
                    _dailyForecastDataStateFlow.value = ApiState.Success(processedForecast)
                }
            } catch (e: Exception) {
                _dailyForecastDataStateFlow.value = ApiState.Failure(e.message ?: "Unknown Error")
            }
        }
    }

    private fun processForecastData(forecastList: List<DailyForecastElement>): List<DailyForecastElement> {
        return forecastList
            .groupBy { element ->
                // Group by day (use LocalDate to avoid considering time)
                Instant.ofEpochSecond(element.dt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .map { (date, dailyElements) ->
                // Find max and min temps for the day
                val maxTemp = dailyElements.maxOf { it.main.temp_max }
                val minTemp = dailyElements.minOf { it.main.temp_min }

                // Use the first element for other fields, or customize as needed
                dailyElements.first().copy(
                    main = dailyElements.first().main.copy(temp_max = maxTemp, temp_min = minTemp)
                )
            }
    }
}