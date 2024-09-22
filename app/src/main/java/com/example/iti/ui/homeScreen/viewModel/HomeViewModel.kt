package com.example.iti.ui.homeScreen.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.model.DailyForecastElement
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class HomeViewModel(private val repository: RepositoryImpl) : ViewModel() {

    private val _weatherDataByCoordinates = MutableLiveData<Result<Weather>>()
    val weatherDataByCoordinates: LiveData<Result<Weather>> get() = _weatherDataByCoordinates

    private val _hourlyForecastDataByCoordinates = MutableLiveData<Result<Hourly>>()
    val hourlyForecastDataByCoordinates: LiveData<Result<Hourly>> get() = _hourlyForecastDataByCoordinates

    // Instead of Result<DailyForecast>, use List<DailyForecastElement> since you're processing the list
    private val _dailyForecastDataByCoordinates = MutableLiveData<List<DailyForecastElement>>()
    val dailyForecastDataByCoordinates: LiveData<List<DailyForecastElement>> get() = _dailyForecastDataByCoordinates


    suspend fun fetchCurrentWeatherDataByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = repository.fetchCurrentWeather(lat, lon)
            _weatherDataByCoordinates.postValue(result)
            Log.d("WeatherRepository", "Weather fetched successfully by coordinates: $result")
        }
    }

    suspend fun fetchHourlyWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = repository.fetchHourlyForecast(lat, lon)
            _hourlyForecastDataByCoordinates.postValue(result)
            Log.d("Hourly", "Hourly forecast fetched successfully: $result")
        }
    }

    suspend fun fetchDailyWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = repository.fetchDailyForecast(lat, lon)
            result.onSuccess { forecast ->
                val processedData =
                    processForecastData(forecast.list)  // Assuming forecast.list is the data
                _dailyForecastDataByCoordinates.postValue(processedData)
            }.onFailure {
                // Handle error
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