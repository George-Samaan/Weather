package com.example.iti.services.pushNotifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.iti.R
import com.example.iti.data.db.remote.RemoteDataSourceImpl
import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.data.network.ApiClient
import com.example.iti.utils.Constants.LATITUDE_SHARED
import com.example.iti.utils.Constants.LONGITUDE_SHARED
import com.example.iti.utils.Constants.SHARED_PREFS_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherNotifyWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @SuppressLint("StringFormatMatches")
    override suspend fun doWork(): Result {
        val lat = inputData.getDouble(LATITUDE_SHARED, 0.0)
        val lon = inputData.getDouble(LONGITUDE_SHARED, 0.0)

        return withContext(Dispatchers.IO) {
            try {
                val weatherRepository =
                    RemoteDataSourceImpl(
                        apiService = ApiClient.retrofit,
                        sharedPrefsDataSource = SharedPrefsDataSourceImpl(
                            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                        )
                    )

                // Collecting the Flow from the repository
                weatherRepository.fetchCurrentWeather(lat, lon).collect { weatherData ->
                    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    when (currentTime) {
                        "08:00" -> showNotification(
                            context.getString(R.string.good_morning),
                            context.getString(
                                R.string.current_temperature_c,
                                weatherData.main.temp
                            ),
                            R.drawable.ic_clear_sky
                        )

                        "16:00" -> showNotification(
                            context.getString(R.string.good_afternoon),
                            context.getString(
                                R.string.current_temperature_c,
                                weatherData.main.temp
                            ),
                            R.drawable.ic_few_cloud
                        )

                        "22:00" -> showNotification(
                            context.getString(R.string.good_evening),
                            context.getString(
                                R.string.current_temperature_c,
                                weatherData.main.temp
                            ),
                            R.drawable.ic_night_hour
                        )

                        else -> {
                            return@collect
                        }
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }


    @SuppressLint("LaunchActivityFromNotification")
    private fun showNotification(title: String, content: String, imageRes: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "WeatherChannel",
                "Weather Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create Notification
        val notification = NotificationCompat.Builder(context, "WeatherChannel")
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setLargeIcon(context.getDrawable(imageRes)?.toBitmap())
            .setAutoCancel(true)
            .build()
        notificationManager.notify((0..1000).random(), notification)
    }
}