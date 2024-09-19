package com.example.iti.db.repository

import com.example.iti.model.Weather

interface Repository {
    suspend fun fetchCurrentWeather(lat: Double, long: Double): Result<Weather>
}