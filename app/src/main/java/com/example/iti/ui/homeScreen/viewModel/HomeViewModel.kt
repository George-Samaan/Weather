package com.example.iti.ui.homeScreen.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: RepositoryImpl) : ViewModel() {

    private val _weatherDataByCoordinates = MutableLiveData<Result<Weather>>()
    val weatherDataByCoordinates: LiveData<Result<Weather>> get() = _weatherDataByCoordinates

    private val _hourlyForecastDataByCoordinates = MutableLiveData<Result<Hourly>>()
    val hourlyForecastDataByCoordinates: LiveData<Result<Hourly>> get() = _hourlyForecastDataByCoordinates

    private val _dailyForecastDataByCoordinates = MutableLiveData<Result<DailyForecast>>()
    val dailyForecastDataByCoordinates: LiveData<Result<DailyForecast>> get() = _dailyForecastDataByCoordinates


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
            _dailyForecastDataByCoordinates.postValue(result)
            Log.d("Daily", "Daily forecast fetched successfully: $result")
        }
    }
}