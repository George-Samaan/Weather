package com.example.iti.repo

import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSource

class FakeSharedPrefsDataSource : SharedPrefsDataSource {
    override fun getTemperatureUnit(): String {
        TODO("Not yet implemented")
    }

    override fun setTemperatureUnit(unit: String) {
        TODO("Not yet implemented")
    }

    override fun getWindSpeedUnit(): String {
        TODO("Not yet implemented")
    }

    override fun setWindSpeedUnit(unit: String) {
        TODO("Not yet implemented")
    }

    override fun saveLocation(latitude: Float, longitude: Float) {
        TODO("Not yet implemented")
    }

    override fun getLocation(): Pair<Float, Float>? {
        TODO("Not yet implemented")
    }

    override fun getNotificationPreference(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setNotificationPreference(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getString(key: String, defaultValue: String): String {
        TODO("Not yet implemented")
    }

    override fun putString(key: String, value: String) {
        TODO("Not yet implemented")
    }
}