package com.example.iti.db.local

import com.example.iti.db.room.WeatherDao
import com.example.iti.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(private val weatherDao: WeatherDao) : LocalDataSource {
    override suspend fun insertWeather(weather: WeatherEntity) {
        return weatherDao.insertWeather(weather)
    }

    override fun getAllWeatherData(): Flow<List<WeatherEntity>> {
        return weatherDao.getAllWeatherData()
    }

    override suspend fun getFirstWeatherItem(): WeatherEntity? {
        return weatherDao.getFirstWeatherItem()
    }

    override suspend fun deleteWeather(weather: WeatherEntity) {
        return weatherDao.deleteWeather(weather)
    }

}