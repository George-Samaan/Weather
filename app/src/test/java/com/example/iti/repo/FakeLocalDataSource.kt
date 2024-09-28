package com.example.iti.repo

import com.example.iti.db.local.favourites.LocalDataSource
import com.example.iti.model.WeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLocalDataSource : LocalDataSource {
    private val weatherList = mutableListOf<WeatherEntity>()

    override suspend fun insertWeather(weather: WeatherEntity) {
        weatherList.add(weather)
    }

    override fun getAllWeatherData(): Flow<List<WeatherEntity>> {
        return flow {
            emit(weatherList)
        }
    }

    override suspend fun deleteWeather(weather: WeatherEntity) {
        weatherList.remove(weather)
    }

    override suspend fun getWeatherCity(cityName: String): WeatherEntity? {
        return weatherList.find { it.cityName == cityName }
    }
}