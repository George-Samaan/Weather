package com.example.iti.db.local.alert

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.iti.alert.AlarmReceiver
import com.example.iti.db.room.AlarmDao
import com.example.iti.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlertDataSourceImpl(
    private val context: Context,
    private val alertDao: AlarmDao,
    private val alarmManager: AlarmManager
) : AlertDataSource {
    @SuppressLint("ScheduleExactAlarm")
    override suspend fun setAlarm(timeInMillis: Long, alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        val alarmEntity = AlarmEntity(timeInMillis = timeInMillis)
        alertDao.insertAlarm(alarmEntity)

    }

    override suspend fun deleteAlarm(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.timeInMillis.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
        alertDao.deleteAlarm(alarm)

    }

    override fun getAllAlarms(): Flow<List<AlarmEntity>> {
        return alertDao.getAllAlarms()

    }
}