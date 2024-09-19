package com.example.iti.db.remote

import com.example.iti.model.Weather

interface RemoteDataSource {
    suspend fun fetchCurrentWeather(lat: Double, long: Double): Result<Weather>
}