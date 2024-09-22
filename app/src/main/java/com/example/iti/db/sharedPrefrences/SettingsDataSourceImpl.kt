package com.example.iti.db.sharedPrefrences

import android.content.SharedPreferences

class SettingsDataSourceImpl(private val sharedPreferences: SharedPreferences) :
    SettingsDataSource {
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
}