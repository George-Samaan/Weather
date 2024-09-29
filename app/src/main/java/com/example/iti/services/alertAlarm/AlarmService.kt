package com.example.iti.services.alertAlarm

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
import com.example.iti.utils.NotificationHelper

class AlarmService : Service() {
    private var overlayView: View? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
            NotificationHelper.dismissNotification(this, 1)
        }
    }

}
