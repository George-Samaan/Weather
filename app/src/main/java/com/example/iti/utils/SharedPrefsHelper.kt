package com.example.iti.utils

import android.content.Context
import android.location.LocationManager
import com.example.iti.utils.Constants.CURRENT_DEVICE_LAT_LANG
import com.example.iti.utils.Constants.HOME_SCREEN_SHARED_PREFS_NAME
import com.example.iti.utils.Constants.LATITUDE_SHARED
import com.example.iti.utils.Constants.LONGITUDE_SHARED

object SharedPrefsHelper {
    fun getLatLonBasedOnLocation(context: Context): Pair<Double, Double> {
        return if (isLocationEnabled(context)) {
            getLatLonFromPrefs(context, CURRENT_DEVICE_LAT_LANG)
        } else {
            getLatLonFromPrefs(context, HOME_SCREEN_SHARED_PREFS_NAME)
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun getLatLonFromPrefs(context: Context, prefsName: String): Pair<Double, Double> {
        val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val lat = sharedPreferences.getFloat(LATITUDE_SHARED, 0.0f).toDouble()
        val lon = sharedPreferences.getFloat(LONGITUDE_SHARED, 0.0f).toDouble()
        return lat to lon
    }
}