package com.example.iti.feature.favourites.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iti.data.model.WeatherEntity
import com.example.iti.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FavouritesViewModel(private val repository: Repository) : ViewModel() {

    val allWeatherData: Flow<List<WeatherEntity>> = repository.getAllWeatherData()

    fun insertWeatherData(weatherEntity: WeatherEntity) {
        viewModelScope.launch {
            repository.insertWeather(weatherEntity)
        }
    }

    fun deleteWeatherData(weatherEntity: WeatherEntity) {
        viewModelScope.launch {
            repository.deleteWeather(weatherEntity)
        }
    }

    suspend fun getWeatherCity(cityName: String): WeatherEntity? {
        return repository.getWeatherCity(cityName)
    }
}