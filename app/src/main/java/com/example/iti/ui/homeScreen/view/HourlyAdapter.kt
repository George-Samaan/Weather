package com.example.iti.ui.homeScreen.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iti.R
import com.example.iti.databinding.ItemHourlyBinding
import com.example.iti.model.HourlyListElement
import com.example.iti.utils.Helpers.formatTime

class HourlyAdapter : ListAdapter<HourlyListElement, HourlyAdapter.HourlyWeatherViewHolder>
    (HourlyWeatherDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val binding = ItemHourlyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        val animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_in_animation)
        holder.itemView.startAnimation(animation)
        holder.bind(getItem(position))
    }

    class HourlyWeatherDiffCallback : DiffUtil.ItemCallback<HourlyListElement>() {
        override fun areItemsTheSame(
            oldItem: HourlyListElement,
            newItem: HourlyListElement
        ): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(
            oldItem: HourlyListElement,
            newItem: HourlyListElement
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class HourlyWeatherViewHolder(private val binding: ItemHourlyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hourlyWeather: HourlyListElement) {

            binding.timeHour.text = formatTime(hourlyWeather.dt)
            binding.tvDegreeDayHour.text = "${hourlyWeather.main.temp.toInt()}°C"
            val hour = getHourFromUnixTime(hourlyWeather.dt)
            val icon = when (hour) {
                6, 9, 12, 15, 18 -> R.drawable.ic_day_hour
                else -> R.drawable.ic_night_hour
            }
            binding.imvWeatherHour.setImageResource(icon)
        }
    }

    private fun getHourFromUnixTime(unixTime: Long): Int {
        val date = java.util.Date(unixTime * 1000L)
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        return calendar.get(java.util.Calendar.HOUR_OF_DAY)  // Get hour in 24-hour format
    }
}