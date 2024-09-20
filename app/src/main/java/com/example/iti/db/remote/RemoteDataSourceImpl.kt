package com.example.iti.db.remote

import android.util.Log
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.network.ApiClient
import com.example.iti.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class RemoteDataSourceImpl : RemoteDataSource {

    private val apiService = ApiClient.retrofit
    override suspend fun fetchCurrentWeather(lat: Double, lon: Double): Result<Weather> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    apiService.getWeather(lat, lon, Constants.API_KEY, Constants.UNITS)
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

    override suspend fun fetchHourlyForecast(lat: Double, lon: Double): Result<Hourly> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    apiService.getHourlyForecast(lat, lon, Constants.API_KEY, Constants.UNITS)
                if (response.isSuccessful && response.body() != null) {
                    Log.d(
                        "WeatherRepository",
                        "Hourly forecast fetched successfully: ${response.body()}"
                    )
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Throwable("Error retrieving hourly forecast data"))
                }
            } catch (e: IOException) {
                Result.failure(Throwable("Network error: ${e.message}"))
            }
        }
    }
}