package com.example.iti.utils

import android.content.Context
import android.view.View
import com.example.iti.R
import com.example.iti.data.model.Weather

object HomeScreenHelper {
    fun checkWeatherDescription(context: Context, weather: Weather): Int {
        val lottieAnimation = when (weather.weather[0].description.lowercase()) {
            context.getString(R.string.clear_sky) -> R.raw.clear_sky_anim
            context.getString(R.string.few_clouds) -> R.raw.few_clouds
            context.getString(R.string.scattered_clouds) -> R.raw.scattered_clouds_anim
            context.getString(R.string.broken_clouds) -> R.raw.broken_cloud_anim
            context.getString(R.string.overcast_clouds) -> R.raw.overcast_clouds_anim
            "light intensity shower rain" -> R.raw.rain_anim    // wait
            context.getString(R.string.light_rain) -> R.raw.rain_anim
            context.getString(R.string.moderate_rain) -> R.raw.rain_anim
            context.getString(R.string.light_snow) -> R.raw.snow_anim
            context.getString(R.string.snow) -> R.raw.snow_anim
            //underTesting
            "thunderstorm" -> R.raw.thunderstorm
            "mist" -> R.raw.mist
            else -> R.raw.clear_sky_anim
        }
        return lottieAnimation
    }

    fun slideInAndScaleView(view: View) {
        if (view.visibility != View.VISIBLE) { // Check if the view is not already visible
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

    fun dynamicTextAnimation(view: View) {
        if (view.visibility != View.VISIBLE) { // Check if the view is not already visible
            view.apply {
                alpha = 0f // Start fully transparent
                scaleX = 0.5f // Start smaller
                scaleY = 0.5f
                rotation = -30f

                visibility = View.VISIBLE

                // Animate with scaling, rotation, and fading
                animate()
                    .alpha(1f) // Fade in
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .rotation(0f)
                    .setDuration(1200)
                    .setInterpolator(android.view.animation.BounceInterpolator()) // Adds a fun bounce effect at the end
                    .withEndAction { // Return to original scale after bounce effect
                        animate()
                            .scaleX(1f) // Return to normal scale
                            .scaleY(1f)
                            .setDuration(300)
                            .start()
                    }
                    .start()
            }
        }
    }

    fun slideInFromLeft(view: View) {
        if (view.visibility != View.VISIBLE) {
            view.apply {
                alpha = 0f
                translationX = -200f
                visibility = View.VISIBLE


                animate()
                    .alpha(1f) // Fade in
                    .translationX(0f)
                    .setDuration(1000) // Animation duration
                    .setInterpolator(android.view.animation.DecelerateInterpolator()) // Smooth deceleration
                    .start()
            }
        }
    }
}