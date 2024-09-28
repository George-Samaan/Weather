package com.example.iti.alert

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.iti.R

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("LaunchActivityFromNotification")
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val alarmIntent = Intent(it, AlarmService::class.java)
            it.startService(alarmIntent)

            createNotification(it)
        }
    }

    private fun createNotification(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channelId = "ALARM_CHANNEL"
                val channel = NotificationChannel(
                    channelId,
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setSound(null, null)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

        val notificationIntent = Intent(context, DismissReceiver::class.java)
         val pendingIntent = PendingIntent.getBroadcast(
             context,
             0,
             notificationIntent,
             PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
         )

         val notification = NotificationCompat.Builder(context, "ALARM_CHANNEL")
             .setSmallIcon(R.drawable.app_logo)
             .setContentText(context.getString(R.string.don_t_forget_to_check_the_weather))
             .setPriority(NotificationCompat.PRIORITY_HIGH)
             .setOngoing(true)
             .addAction(R.drawable.ic_notification, "Dismiss", pendingIntent)
             .setSound(null)
             .build()

         val notificationManager =
             context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
         notificationManager.notify(1, notification)
    }

}