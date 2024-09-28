package com.example.iti.db.sharedPrefrences

import android.content.SharedPreferences

class SharedPrefsDataSourceImpl(private val sharedPreferences: SharedPreferences) :
    SharedPrefsDataSource {
    override fun getTemperatureUnit(): String {
        return sharedPreferences.getString("TempUnit", "Celsius") ?: "Celsius"
    }

    override fun setTemperatureUnit(unit: String) {
        sharedPreferences.edit().putString("TempUnit", unit).apply()
    }

    override fun getWindSpeedUnit(): String {
        return sharedPreferences.getString("wind_speed_unit", "Meter/Second") ?: "Meter/Second"
    }

    override fun setWindSpeedUnit(unit: String) {
        sharedPreferences.edit().putString("wind_speed_unit", unit).apply()
    }

    override fun saveLocation(latitude: Float, longitude: Float) {
        with(sharedPreferences.edit()) {
            putFloat("latitude", latitude)
            putFloat("longitude", longitude)
            apply()
        }
    }

    override fun getLocation(): Pair<Float, Float>? {
        val latitude = sharedPreferences.getFloat("latitude", Float.NaN)
        val longitude = sharedPreferences.getFloat("longitude", Float.NaN)
        return if (!latitude.isNaN() && !longitude.isNaN()) {
            Pair(latitude, longitude)
        } else {
            null
        }
    }

    override fun getNotificationPreference(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    override fun setNotificationPreference(enabled: Boolean) {
        return sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}