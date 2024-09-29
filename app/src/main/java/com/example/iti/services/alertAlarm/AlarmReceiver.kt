package com.example.iti.services.alertAlarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.iti.utils.Constants.HOME_SCREEN_SHARED_PREFS_NAME
import com.example.iti.utils.Constants.LATITUDE_SHARED
import com.example.iti.utils.Constants.LONGITUDE_SHARED
import com.example.iti.utils.SharedPrefsHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            // Starting alarm sound service
            val alarmIntent = Intent(it, AlarmService::class.java)
            it.startService(alarmIntent)

            // Fetch the weather and show a notification with the real-time data
            fetchWeatherAndShowNotification(it)
        }
    }

    private fun fetchWeatherAndShowNotification(context: Context) {
        val (lat, lon) = SharedPrefsHelper.getLatLonFromPrefs(
            context,
            HOME_SCREEN_SHARED_PREFS_NAME
        )

        // Create input data for the worker
        val inputData = Data.Builder()
            .putDouble(LATITUDE_SHARED, lat)
            .putDouble(LONGITUDE_SHARED, lon)
            .build()

        // Create a one-time work request to fetch the weather data
        val weatherRequest = OneTimeWorkRequestBuilder<WeatherAlarmWorker>()
            .setInputData(inputData)
            .build()
        // Enqueue the request
        WorkManager.getInstance(context).enqueue(weatherRequest)


    }
}