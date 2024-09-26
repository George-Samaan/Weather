package com.example.iti.db.remote

import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.network.ApiServices
import com.example.iti.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteDataSourceImpl(private val apiService: ApiServices) : RemoteDataSource {


    override fun fetchCurrentWeather(lat: Double, lon: Double): Flow<Weather> = flow {
        val response = apiService.getWeather(lat, lon, Constants.API_KEY, Constants.UNITS)
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!)
        } else {
            throw Throwable("Error retrieving weather data")
        }
    }

    override fun fetchHourlyForecast(lat: Double, lon: Double): Flow<Hourly> = flow {
        val response = apiService.getHourlyForecast(lat, lon, Constants.API_KEY, Constants.UNITS)
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!)
        } else {
            throw Throwable("Error retrieving hourly forecast data")
        }
    }

    override fun fetchDailyForecast(lat: Double, lon: Double): Flow<DailyForecast> = flow {
        val response = apiService.getDailyForecast(lat, lon, Constants.API_KEY, Constants.UNITS)
        if (response.isSuccessful && response.body() != null) {
            emit(response.body()!!)
        } else {
            throw Throwable("Error retrieving daily forecast data")
        }
    }
}