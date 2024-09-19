package com.example.iti.db.repository

import com.example.iti.db.remote.RemoteDataSource
import com.example.iti.model.Weather

class RepositoryImpl(private val remoteDataSource: RemoteDataSource) : Repository {

    override suspend fun fetchCurrentWeather(lat: Double, long: Double): Result<Weather> {
        return remoteDataSource.fetchCurrentWeather(lat, long)
    }
}