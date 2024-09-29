package com.example.iti.data.repository

import com.example.iti.data.model.DailyForecast
import com.example.iti.data.model.Hourly
import com.example.iti.data.model.Weather
import com.example.iti.data.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface Repository {
    //remote methods
    fun fetchCurrentWeather(lat: Double, long: Double): Flow<Weather>
    fun fetchHourlyForecast(lat: Double, lon: Double): Flow<Hourly>
    fun fetchDailyForecast(lat: Double, lon: Double): Flow<DailyForecast>

    //SharedPrefs Methods
    fun getTemperatureUnit(): String
    fun setTemperatureUnit(unit: String)
    fun getWindSpeedUnit(): String
    fun setWindSpeedUnit(unit: String)
    fun saveLocation(latitude: Float, longitude: Float)
    fun getLocation(): Pair<Float, Float>?
    fun getNotificationPreference(): Boolean
    fun setNotificationPreference(enabled: Boolean)
    fun getLanguage(): String
    fun setLanguage(language: String)

    //local database
    suspend fun insertWeather(weather: WeatherEntity)
    fun getAllWeatherData(): Flow<List<WeatherEntity>>
    suspend fun deleteWeather(weather: WeatherEntity)
    suspend fun getWeatherCity(cityName: String): WeatherEntity?

}