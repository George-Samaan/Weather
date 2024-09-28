package com.example.iti.repo

import com.example.iti.db.remote.RemoteDataSource
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import kotlinx.coroutines.flow.Flow

class FakeRemoteDataSource : RemoteDataSource {
    override fun fetchCurrentWeather(lat: Double, long: Double): Flow<Weather> {
        TODO("Not yet implemented")
    }

    override fun fetchHourlyForecast(lat: Double, lon: Double): Flow<Hourly> {
        TODO("Not yet implemented")
    }

    override fun fetchDailyForecast(lat: Double, lon: Double): Flow<DailyForecast> {
        TODO("Not yet implemented")
    }
}