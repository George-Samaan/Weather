package com.example.iti.data.db.local.favourites

import com.example.iti.data.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun insertWeather(weather: WeatherEntity)
    fun getAllWeatherData(): Flow<List<WeatherEntity>>
    suspend fun deleteWeather(weather: WeatherEntity)
    suspend fun getWeatherCity(cityName: String): WeatherEntity?
}