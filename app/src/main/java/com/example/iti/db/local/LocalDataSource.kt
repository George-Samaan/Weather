package com.example.iti.db.local

import com.example.iti.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun insertWeather(weather: WeatherEntity)
    fun getAllWeatherData(): Flow<List<WeatherEntity>>
    suspend fun getFirstWeatherItem(): WeatherEntity?
    suspend fun deleteWeather(weather: WeatherEntity)
    suspend fun getWeatherCity(cityName: String): WeatherEntity?
}