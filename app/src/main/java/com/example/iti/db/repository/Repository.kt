package com.example.iti.db.repository

import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather

interface Repository {
    suspend fun fetchCurrentWeather(lat: Double, long: Double): Result<Weather>
    suspend fun fetchHourlyForecast(lat: Double, lon: Double): Result<Hourly>
    suspend fun fetchDailyForecast(lat: Double, lon: Double): Result<DailyForecast>

    //SharedPrefs Methods
    fun getTemperatureUnit(): String
    fun setTemperatureUnit(unit: String)
    fun getWindSpeedUnit(): String
    fun setWindSpeedUnit(unit: String)

}