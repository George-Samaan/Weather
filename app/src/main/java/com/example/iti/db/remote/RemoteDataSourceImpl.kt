package com.example.iti.db.remote

import android.util.Log
import com.example.iti.model.Weather
import com.example.iti.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class RemoteDataSourceImpl : RemoteDataSource {

    private val apiService = ApiClient.retrofit
    override suspend fun fetchCurrentWeather(lat: Double, lon: Double): Result<Weather> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    apiService.getWeather(lat, lon, "d8b0d405f9d41f5903ec35720dfdb84c", "metric")
                if (response.isSuccessful && response.body() != null) {
                    Log.d(
                        "WeatherRepository",
                        "Weather fetched successfully by coordinates: ${response.body()}"
                    )
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Throwable("Error retrieving weather data by coordinates"))
                }
            } catch (e: IOException) {
                Result.failure(Throwable("Network error: ${e.message}"))
            }
        }
    }
}