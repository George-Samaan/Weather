package com.example.iti.db.sharedPrefrences

interface SettingsDataSource {
    fun getTemperatureUnit(): String
    fun setTemperatureUnit(unit: String)

    fun getWindSpeedUnit(): String
    fun setWindSpeedUnit(unit: String)
}