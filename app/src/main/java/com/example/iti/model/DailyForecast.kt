package com.example.iti.model

data class DailyForecast(
    val list: List<DailyForecastElement>,
    val city: City
)

data class DailyForecastElement(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherElement>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val sys: Sys,
    val dt_txt: String
)



