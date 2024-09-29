package com.example.iti.data.db.local.alert

import com.example.iti.data.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlertDataSource {
    suspend fun setAlarm(timeInMillis: Long, alarmId: Int)
    suspend fun deleteAlarm(alarm: AlarmEntity)
    fun getAllAlarms(): Flow<List<AlarmEntity>>
}