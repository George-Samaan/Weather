package com.example.iti.ui.settings.viewModel

import com.example.iti.db.repository.Repository
import com.example.iti.model.DailyForecast
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.model.WeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRepository : Repository {
    private val weatherData = mutableListOf<WeatherEntity>()

    private var temperatureUnit: String = "Celsius"
    private var windSpeedUnit: String = "km/h"
    private var notificationPreference: Boolean = true

    override fun fetchCurrentWeather(lat: Double, long: Double): Flow<Weather> {
        TODO("Not yet implemented")
    }

    override fun fetchHourlyForecast(lat: Double, lon: Double): Flow<Hourly> {
        TODO("Not yet implemented")
    }

    override fun fetchDailyForecast(lat: Double, lon: Double): Flow<DailyForecast> {
        TODO("Not yet implemented")
    }

    override fun getTemperatureUnit(): String {
        return temperatureUnit
    }

    override fun setTemperatureUnit(unit: String) {
        temperatureUnit = unit
    }

    override fun getWindSpeedUnit(): String {
        return windSpeedUnit
    }

    override fun setWindSpeedUnit(unit: String) {
        windSpeedUnit = unit
    }

    override fun getNotificationPreference(): Boolean {
        return notificationPreference
    }

    override fun setNotificationPreference(enabled: Boolean) {
        notificationPreference = enabled
    }


    override fun saveLocation(latitude: Float, longitude: Float) {
        TODO("Not yet implemented")
    }

    override fun getLocation(): Pair<Float, Float>? {
        TODO("Not yet implemented")
    }

    override fun getLanguage(): String {
        TODO("Not yet implemented")
    }

    override fun setLanguage(language: String) {
        TODO("Not yet implemented")
    }

    override suspend fun insertWeather(weather: WeatherEntity) {
        weatherData.add(weather)
    }

    override fun getAllWeatherData(): Flow<List<WeatherEntity>> = flow {
        emit(weatherData)
    }

    override suspend fun deleteWeather(weather: WeatherEntity) {
        weatherData.remove(weather)
    }

    override suspend fun getWeatherCity(cityName: String): WeatherEntity? {
        return weatherData.find { it.cityName == cityName }
    }
}