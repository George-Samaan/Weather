package com.example.iti.db.repository

import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface Repository {
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



    //local database
    suspend fun insertWeather(weather: WeatherEntity)
    fun getAllWeatherData(): Flow<List<WeatherEntity>>
    suspend fun deleteWeather(weather: WeatherEntity)
    suspend fun getWeatherCity(cityName: String): WeatherEntity?

}