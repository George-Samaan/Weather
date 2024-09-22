package com.example.iti.ui.homeScreen.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iti.R
import com.example.iti.databinding.ItemDailyBinding
import com.example.iti.model.DailyForecastElement
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.utils.Helpers.convertTemperature
import com.example.iti.utils.Helpers.getUnitSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyAdapter(
    private val settingsViewModel: SettingsViewModel,
    private val lifecycleScope: CoroutineScope
) :
    ListAdapter<DailyForecastElement, DailyAdapter.DailyWeatherViewHolder>(DailyWeatherDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DailyAdapter.DailyWeatherViewHolder {
        val binding = ItemDailyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyAdapter.DailyWeatherViewHolder, position: Int) {
        val animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_in_animation)
        holder.bind(getItem(position))
        holder.itemView.startAnimation(animation)
    }

    inner class DailyWeatherViewHolder(private val binding: ItemDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("DefaultLocale")
        fun bind(dailyWeather: DailyForecastElement) {
            lifecycleScope.launch(Dispatchers.Main) {
                val unit = settingsViewModel.getTemperatureUnit()

                // Convert the timestamp to a LocalDate
                val date = Instant.ofEpochSecond(dailyWeather.dt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                // Check if the date is today
                val today = LocalDate.now()
                val dayString = when (date) {
                    today -> "Today"
                    today.plusDays(1) -> "Tomorrow"
                    else -> date.format(
                        DateTimeFormatter.ofPattern(
                            "EEEE",
                            Locale.getDefault()
                        )
                    ) // Day of the week
                }
                val maxTemp = convertTemperature(dailyWeather.main.temp_max, unit)
                val minTemp = convertTemperature(dailyWeather.main.temp_min, unit)
                binding.tvDayDays.text = dayString

                // Set weather details
                binding.tvWeatherCondition.text = dailyWeather.weather[0].description
                binding.tvHighDegree.text = String.format("%.0f", maxTemp, getUnitSymbol(unit))
                binding.tvLowDegree.text = String.format("%.0f°%s", minTemp, getUnitSymbol(unit))

                val iconCode = dailyWeather.weather[0].icon
                binding.ivIconDays.setImageResource(getCustomIconForWeather(iconCode))
            }
        }
    }

    private fun getCustomIconForWeather(iconCode: String): Int {
        return when (iconCode) {
            "01d", "01n" -> R.drawable.ic_clear_sky
            "02d", "02n" -> R.drawable.ic_few_cloud
            "03d", "03n" -> R.drawable.ic_scattered_clouds
            "04d", "04n" -> R.drawable.ic_broken_clouds
            "09d", "09n" -> R.drawable.ic_shower_rain
            "10d", "10n" -> R.drawable.ic_rain
            "11d", "11n" -> R.drawable.ic_thunderstorm
            "13d", "13n" -> R.drawable.ic_snow
            "50d", "50n" -> R.drawable.ic_mist
            else -> R.drawable.ic_clear_sky
        }
    }

    class DailyWeatherDiffCallback : DiffUtil.ItemCallback<DailyForecastElement>() {
        override fun areItemsTheSame(
            oldItem: DailyForecastElement,
            newItem: DailyForecastElement
        ): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(
            oldItem: DailyForecastElement,
            newItem: DailyForecastElement
        ): Boolean {
            return oldItem == newItem
        }
    }
}



