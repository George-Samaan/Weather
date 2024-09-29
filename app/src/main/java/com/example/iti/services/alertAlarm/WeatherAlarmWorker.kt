package com.example.iti.services.alertAlarm

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.iti.R
import com.example.iti.data.db.remote.RemoteDataSourceImpl
import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.data.network.ApiClient
import com.example.iti.utils.Constants.HOME_SCREEN_SHARED_PREFS_NAME
import com.example.iti.utils.NotificationHelper
import com.example.iti.utils.SharedPrefsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherAlarmWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @SuppressLint("StringFormatMatches")
    override suspend fun doWork(): Result {
        val (lat, lon) = SharedPrefsHelper.getLatLonBasedOnLocation(context)

        return withContext(Dispatchers.IO) {
            try {
                val weatherRepository = RemoteDataSourceImpl(
                    apiService = ApiClient.retrofit,
                    sharedPrefsDataSource = SharedPrefsDataSourceImpl(
                        context.getSharedPreferences(
                            HOME_SCREEN_SHARED_PREFS_NAME,
                            Context.MODE_PRIVATE
                        )
                    )
                )

                // Collect the weather data from the repository
                weatherRepository.fetchCurrentWeather(lat, lon).collect { weatherData ->
                    // Show a notification with the fetched weather data
                    NotificationHelper.showNotification(
                        context,
                        "Weather Update -> ${weatherData.weather[0].description}",
                        context.getString(R.string.current_temperature_c, weatherData.main.temp),
                        R.drawable.app_logo
                    )
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}