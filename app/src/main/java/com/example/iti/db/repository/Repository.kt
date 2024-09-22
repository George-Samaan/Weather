package com.example.iti.db.repository

import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
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

}