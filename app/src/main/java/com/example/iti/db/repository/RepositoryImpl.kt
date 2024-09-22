package com.example.iti.db.repository

import com.example.iti.db.remote.RemoteDataSource
import com.example.iti.db.sharedPrefrences.SettingsDataSource
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import kotlinx.coroutines.flow.Flow

class RepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val settingsDataSource: SettingsDataSource
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

}