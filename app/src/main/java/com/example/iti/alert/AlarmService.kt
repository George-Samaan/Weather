package com.example.iti.alert

import android.app.NotificationManager
import android.app.Service
import android.content.Context
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

class AlarmService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var mediaPlayer: MediaPlayer? = null
    private var notificationManager: NotificationManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_clock)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
        showAlarmOverlay()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return START_STICKY
        // Ensures the service continues to run if the system kills it for memory reasons.
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun showAlarmOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater = LayoutInflater.from(this)

        overlayView = layoutInflater.inflate(R.layout.item_background_alert, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        windowManager?.addView(overlayView, params)

        overlayView?.findViewById<Button>(R.id.dismiss_button)?.setOnClickListener {
            stopSelf()
            windowManager?.removeView(overlayView)

            notificationManager?.cancel(1)
        }
    }

}
