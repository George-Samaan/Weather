package com.example.iti.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helpers {
    fun celsiusToFahrenheit(celsius: Int): Int {
        return (celsius * 9 / 5) + 32
    }

    fun celsiusToKelvin(celsius: Int): Int {
        return celsius + 273
    }

    fun decideUint(unit: String): String {
        when (unit) {
            "metric" -> return "°C"
            "imperial" -> return "°F"
            else -> return "°K"
        }
    }

    fun meterPerSecondToMilePerHour(meter: Double): Double {
        return meter * 2.23694
    }

    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp * 1000))
    }
}