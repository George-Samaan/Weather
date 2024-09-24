package com.example.iti.utils

import android.view.View
import com.example.iti.R
import com.example.iti.model.Weather

object HomeScreenHelper {
    fun checkWeatherDescription(weather: Weather): Int {
        val lottieAnimation = when (weather.weather[0].description.lowercase()) {
            "clear sky" -> R.raw.clear_sky_anim
            "few clouds" -> R.raw.few_clouds
            "scattered clouds" -> R.raw.scattered_clouds_anim
            "broken clouds" -> R.raw.broken_cloud_anim
            "overcast clouds" -> R.raw.overcast_clouds_anim
            "light intensity shower rain" -> R.raw.rain_anim
            "light rain" -> R.raw.rain_anim
            "moderate rain" -> R.raw.rain_anim
            "light snow" -> R.raw.snow_anim
            //underTesting
            "thunderstorm" -> R.raw.thunderstorm
            "mist" -> R.raw.mist
            else -> R.raw.clear_sky_anim
        }
        return lottieAnimation
    }

    fun slideInAndScaleView(view: View) {
        view.apply {
            scaleX = 0f
            scaleY = 0f
            translationY = 100f
            visibility = View.VISIBLE
            // Animate scaling and sliding in
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(1000)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }


}