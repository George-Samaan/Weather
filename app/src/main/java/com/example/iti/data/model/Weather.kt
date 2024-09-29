package com.example.iti.data.model

data class Weather(
    val visibility: Long,
    val timezone: Long,
    val main: Main,
    val clouds: Clouds,
    val sys: Sys,
    val dt: Long,
    val coord: Coord,
    val weather: List<WeatherElement>,
    val name: String,
    val cod: Long,
    val id: Long,
    val base: String,
    val wind: Wind
)

data class Clouds(
    val all: Long
)

data class Coord(
    val lon: Double,
    val lat: Double
)

data class Main(
    val feels_like: Double,
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long,
    val id: Long,
    val type: Long
)

data class WeatherElement(
    val icon: String,
    val description: String,
    val main: String,
    val id: Long
)

data class Wind(
    val deg: Long,
    val speed: Double,
    val gust: Double
)
