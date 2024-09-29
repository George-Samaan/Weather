package com.example.iti.data.db.local.favourites

import com.example.iti.data.db.room.WeatherDao
import com.example.iti.data.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(private val weatherDao: WeatherDao) : LocalDataSource {
    override suspend fun insertWeather(weather: WeatherEntity) {
        return weatherDao.insertWeather(weather)
    }

    override fun getAllWeatherData(): Flow<List<WeatherEntity>> {
        return weatherDao.getAllWeatherData()
    }

    override suspend fun deleteWeather(weather: WeatherEntity) {
        return weatherDao.deleteWeather(weather)
    }

    override suspend fun getWeatherCity(cityName: String): WeatherEntity? {
        return weatherDao.getWeatherByCity(cityName)
    }

}