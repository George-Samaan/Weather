package com.example.iti.db.repository

import com.example.iti.db.remote.RemoteDataSource
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather

class RepositoryImpl(private val remoteDataSource: RemoteDataSource) : Repository {

    override suspend fun fetchCurrentWeather(lat: Double, long: Double): Result<Weather> {
        return remoteDataSource.fetchCurrentWeather(lat, long)
    }

    override suspend fun fetchHourlyForecast(lat: Double, lon: Double): Result<Hourly> {
        return remoteDataSource.fetchHourlyForecast(lat, lon)
    }

    override suspend fun fetchDailyForecast(lat: Double, lon: Double): Result<DailyForecast> {
        return remoteDataSource.fetchDailyForecast(lat, lon)
    }

}