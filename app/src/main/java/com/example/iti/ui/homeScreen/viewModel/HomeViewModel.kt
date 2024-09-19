package com.example.iti.ui.homeScreen.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.model.Weather
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: RepositoryImpl) : ViewModel() {

    private val _weatherDataByCoordinates = MutableLiveData<Result<Weather>>()
    val weatherDataByCoordinates: LiveData<Result<Weather>> get() = _weatherDataByCoordinates


    suspend fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = repository.fetchCurrentWeather(lat, lon)
            _weatherDataByCoordinates.postValue(result)
            Log.d("WeatherRepository", "Weather fetched successfully by coordinates: $result")
        }
    }
}