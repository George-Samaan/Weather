package com.example.iti.db.sharedPrefrences

interface SharedPrefsDataSource {
    fun getTemperatureUnit(): String
    fun setTemperatureUnit(unit: String)

    fun getWindSpeedUnit(): String
    fun setWindSpeedUnit(unit: String)

    fun saveLocation(latitude: Float, longitude: Float)
    fun getLocation(): Pair<Float, Float>?
}