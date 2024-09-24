package com.example.iti.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val cityName: String,
    val description: String,
    val currentTemp: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val clouds: Int,
    val sunrise: Long,
    val sunset: Long,
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val lottie: Int
)