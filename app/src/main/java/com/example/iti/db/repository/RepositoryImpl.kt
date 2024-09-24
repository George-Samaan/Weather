package com.example.iti.db.repository

import com.example.iti.db.local.LocalDataSource
import com.example.iti.db.remote.RemoteDataSource
import com.example.iti.db.sharedPrefrences.SettingsDataSource
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

class RepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val settingsDataSource: SettingsDataSource,
    private val localDataSource: LocalDataSource
) : Repository {

    override fun fetchCurrentWeather(lat: Double, long: Double): Flow<Weather> {
        return remoteDataSource.fetchCurrentWeather(lat, long)
    }

    override fun fetchHourlyForecast(lat: Double, lon: Double): Flow<Hourly> {
        return remoteDataSource.fetchHourlyForecast(lat, lon)
    }

    override fun fetchDailyForecast(lat: Double, lon: Double): Flow<DailyForecast> {
        return remoteDataSource.fetchDailyForecast(lat, lon)
    }

    // SharedPrefs Methods

    override fun getTemperatureUnit(): String {
        return settingsDataSource.getTemperatureUnit()
    }

    override fun setTemperatureUnit(unit: String) {
        return settingsDataSource.setTemperatureUnit(unit)
    }

    override fun getWindSpeedUnit(): String {
        return settingsDataSource.getWindSpeedUnit()
    }

    override fun setWindSpeedUnit(unit: String) {
        return settingsDataSource.setWindSpeedUnit(unit)
    }

    override suspend fun insertWeather(weather: WeatherEntity) {
        return localDataSource.insertWeather(weather)
    }

    override fun getAllWeatherData(): Flow<List<WeatherEntity>> {
        return localDataSource.getAllWeatherData()
    }

    override suspend fun getFirstWeatherItem(): WeatherEntity? {
        return localDataSource.getFirstWeatherItem()
    }

    override suspend fun deleteWeather(weather: WeatherEntity) {
        return localDataSource.deleteWeather(weather)
    }

    override suspend fun getWeatherCity(cityName: String): WeatherEntity? {
        return localDataSource.getWeatherCity(cityName)
    }

}