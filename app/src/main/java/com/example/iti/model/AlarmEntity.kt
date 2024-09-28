package com.example.iti.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) // Add this
    val alarmId: Int = 0, // Make alarmId the primary key
    val timeInMillis: Long
)