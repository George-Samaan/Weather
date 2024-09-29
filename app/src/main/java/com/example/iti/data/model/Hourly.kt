package com.example.iti.data.model

data class Hourly(
    val city: City,
    val list: List<HourlyListElement>
)

data class HourlyListElement(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherElement>,
    val wind: Wind
)

data class City(
    val country: String,
    val coord: Coord,
    val sunrise: Long,
    val timezone: Long,
    val sunset: Long,
    val name: String,
    val id: Long,
    val population: Long
)