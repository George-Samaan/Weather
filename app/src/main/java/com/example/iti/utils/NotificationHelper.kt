package com.example.iti.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.iti.R

object NotificationHelper {
    @SuppressLint("UseCompatLoadingForDrawables")
    fun showNotification(
        context: Context,
        title: String,
        content: String,
        imageRes: Int,
        id: Int = (0..1000).random()
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "WeatherAlarmChannel",
                "Weather Alarm Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "WeatherAlarmChannel")
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setLargeIcon(context.getDrawable(imageRes)?.toBitmap())
            .setAutoCancel(true)
            .build()
        notificationManager.notify(id, notification)
    }

    fun dismissNotification(context: Context, id: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)

    }
}