package com.example.iti.db.repository

import com.example.iti.db.local.favourites.LocalDataSource
import com.example.iti.db.remote.RemoteDataSource
import com.example.iti.db.sharedPrefrences.SharedPrefsDataSource
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.model.WeatherEntity
import com.example.iti.utils.Constants.ENGLISH_SHARED
import com.example.iti.utils.Constants.LANGUAGE_SHARED
import kotlinx.coroutines.flow.Flow

class RepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val sharedPrefsDataSource: SharedPrefsDataSource,
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
        return sharedPrefsDataSource.getTemperatureUnit()
    }

    override fun setTemperatureUnit(unit: String) {
        return sharedPrefsDataSource.setTemperatureUnit(unit)
    }

    override fun getWindSpeedUnit(): String {
        return sharedPrefsDataSource.getWindSpeedUnit()
    }

    override fun setWindSpeedUnit(unit: String) {
        return sharedPrefsDataSource.setWindSpeedUnit(unit)
    }

    override fun saveLocation(latitude: Float, longitude: Float) {
        sharedPrefsDataSource.saveLocation(latitude, longitude)
    }

    override fun getLocation(): Pair<Float, Float>? {
        return sharedPrefsDataSource.getLocation()
    }
    override fun getNotificationPreference(): Boolean {
        return sharedPrefsDataSource.getNotificationPreference()
    }

    override fun setNotificationPreference(enabled: Boolean) {
        return sharedPrefsDataSource.setNotificationPreference(enabled)
    }
    override fun getLanguage(): String {
        return sharedPrefsDataSource.getString(LANGUAGE_SHARED, ENGLISH_SHARED)
    }

    override fun setLanguage(language: String) {
        sharedPrefsDataSource.putString(LANGUAGE_SHARED, language)
    }


    //Local DataSource

    override suspend fun insertWeather(weather: WeatherEntity) {
        return localDataSource.insertWeather(weather)
    }

    override fun getAllWeatherData(): Flow<List<WeatherEntity>> {
        return localDataSource.getAllWeatherData()
    }

    override suspend fun deleteWeather(weather: WeatherEntity) {
        return localDataSource.deleteWeather(weather)
    }

    override suspend fun getWeatherCity(cityName: String): WeatherEntity? {
        return localDataSource.getWeatherCity(cityName)
    }
}