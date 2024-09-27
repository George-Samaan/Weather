//package com.example.iti.alert
//
//import android.app.NotificationManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//
//class DismissReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        context?.let {
//            val stopIntent = Intent(it, AlarmService::class.java)
//            it.stopService(stopIntent)
//
//
///*            val notificationManager =
//                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.cancel(1)*/
//        }
//    }
//}