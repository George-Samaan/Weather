package com.example.iti.data.db.sharedPrefrences

interface SharedPrefsDataSource {
    fun getTemperatureUnit(): String
    fun setTemperatureUnit(unit: String)

    fun getWindSpeedUnit(): String
    fun setWindSpeedUnit(unit: String)

    fun saveLocation(latitude: Float, longitude: Float)
    fun getLocation(): Pair<Float, Float>?

    fun getNotificationPreference(): Boolean
    fun setNotificationPreference(enabled: Boolean)

    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)

}