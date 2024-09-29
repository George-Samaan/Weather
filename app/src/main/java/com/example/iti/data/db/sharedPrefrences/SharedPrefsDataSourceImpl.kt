package com.example.iti.data.db.sharedPrefrences

import android.content.SharedPreferences
import com.example.iti.utils.Constants
import com.example.iti.utils.Constants.CELSIUS_SHARED
import com.example.iti.utils.Constants.LATITUDE_SHARED
import com.example.iti.utils.Constants.LONGITUDE_SHARED
import com.example.iti.utils.Constants.NOTIFICATION_SHARED
import com.example.iti.utils.Constants.TEMP_UNIT_SHARED
import com.example.iti.utils.Constants.WIND_SPEED_UNIT_SHARED

class SharedPrefsDataSourceImpl(private val sharedPreferences: SharedPreferences) :
    SharedPrefsDataSource {
    override fun getTemperatureUnit(): String {
        return sharedPreferences.getString(TEMP_UNIT_SHARED, CELSIUS_SHARED) ?: CELSIUS_SHARED
    }

    override fun setTemperatureUnit(unit: String) {
        sharedPreferences.edit().putString(TEMP_UNIT_SHARED, unit).apply()
    }

    override fun getWindSpeedUnit(): String {
        return sharedPreferences.getString(WIND_SPEED_UNIT_SHARED, Constants.METER_PER_SECOND)
            ?: Constants.METER_PER_SECOND
    }

    override fun setWindSpeedUnit(unit: String) {
        sharedPreferences.edit().putString(WIND_SPEED_UNIT_SHARED, unit).apply()
    }

    override fun saveLocation(latitude: Float, longitude: Float) {
        with(sharedPreferences.edit()) {
            putFloat(LATITUDE_SHARED, latitude)
            putFloat(LONGITUDE_SHARED, longitude)
            apply()
        }
    }

    override fun getLocation(): Pair<Float, Float>? {
        val latitude = sharedPreferences.getFloat(LATITUDE_SHARED, Float.NaN)
        val longitude = sharedPreferences.getFloat(LONGITUDE_SHARED, Float.NaN)
        return if (!latitude.isNaN() && !longitude.isNaN()) {
            Pair(latitude, longitude)
        } else {
            null
        }
    }

    override fun getNotificationPreference(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATION_SHARED, true)
    }

    override fun setNotificationPreference(enabled: Boolean) {
        return sharedPreferences.edit().putBoolean(NOTIFICATION_SHARED, enabled).apply()
    }

    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}