package com.example.iti.services.alertAlarm

import android.app.AlarmManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import com.example.iti.R
import com.example.iti.data.db.local.alert.AlertDataSourceImpl
import com.example.iti.data.db.room.AppDatabase
import com.example.iti.data.model.AlarmEntity
import com.example.iti.utils.Constants.ALARM_ID_TO_DISMISS
import com.example.iti.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {
    private var overlayView: View? = null
    private var mediaPlayer: MediaPlayer? = null
    private var alarmId = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmId = intent?.getIntExtra(ALARM_ID_TO_DISMISS, -1) ?: -1
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_clock).apply {
            isLooping = true
            start()
        }
        showAlarmOverlay()
        return START_STICKY
        // Ensures the service continues to run if the system kills it for memory reasons.
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()

        // Remove the overlay if it exists
        overlayView?.let {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            overlayView = null
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun showAlarmOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.item_background_alert, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        windowManager.addView(overlayView, params)

        overlayView?.findViewById<Button>(R.id.dismiss_button)?.setOnClickListener {
            stopSelf()
            windowManager.removeView(overlayView)
            // Call the method to delete the alarm from the database
            if (alarmId != -1) {
                // Use the AlarmDataSource to delete the alarm
                val alertDataSource = AlertDataSourceImpl(
                    this,
                    AppDatabase.getDatabase(this).alarmDao(),
                    getSystemService(ALARM_SERVICE) as AlarmManager
                )
                // Launch a coroutine to delete the alarm
                CoroutineScope(Dispatchers.IO).launch {
                    alertDataSource.deleteAlarm(AlarmEntity(alarmId = alarmId, timeInMillis = 0))
                }
            }

            NotificationHelper.dismissNotification(this, 1)
        }
    }

}
