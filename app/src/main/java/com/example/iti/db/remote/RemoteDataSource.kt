package com.example.iti.db.remote

import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun fetchCurrentWeather(lat: Double, long: Double): Flow<Weather>
    fun fetchHourlyForecast(lat: Double, lon: Double): Flow<Hourly>
    fun fetchDailyForecast(lat: Double, lon: Double): Flow<DailyForecast>
}