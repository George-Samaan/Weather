package com.example.iti.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helpers {
    fun convertTemperature(tempInCelsius: Double, unit: String): Double {
        return when (unit) {
            "Celsius" -> tempInCelsius
            "Fahrenheit" -> (tempInCelsius * 9 / 5) + 32
            "Kelvin" -> tempInCelsius + 273.15
            else -> tempInCelsius
        }
    }

    fun getUnitSymbol(unit: String): String {
        return when (unit) {
            "Celsius" -> "C"
            "Fahrenheit" -> "F"
            "Kelvin" -> "K"
            else -> "C"
        }
    }

    fun getWindSpeedUnitSymbol(unit: String): String {
        return when (unit) {
            "Meter/Second" -> "m/s"
            "Miles/Hour" -> "mph"
            else -> "m/s"
        }
    }

    fun convertWindSpeed(speed: Double, fromUnit: String, toUnit: String): Double {
        return when (toUnit) {
            "Miles/Hour" -> if (fromUnit == "Meter/Second") speed * 2.23694 else speed
            "Meter/Second" -> if (fromUnit == "Miles/Hour") speed / 2.23694 else speed
            else -> speed
        }
    }
    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp * 1000))
    }

    @SuppressLint("SimpleDateFormat")
    fun date(): String {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    fun getHourFromUnixTime(unixTime: Long): Int {
        val date = Date(unixTime * 1000L)
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        return calendar.get(java.util.Calendar.HOUR_OF_DAY)  // Get hour in 24-hour format
    }

    // Function to check network availability
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    fun formatDatePlusYears(timeInMillis: Long): String {
        return SimpleDateFormat("hh:mm a, MMM dd yyyy", Locale.getDefault()).format(
            Date(
                timeInMillis
            )
        )
    }

}