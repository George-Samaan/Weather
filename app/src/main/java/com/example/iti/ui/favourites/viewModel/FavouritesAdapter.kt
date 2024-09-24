package com.example.iti.ui.favourites.viewModel

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iti.R
import com.example.iti.databinding.ItemFavouriteBinding
import com.example.iti.model.WeatherEntity
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.utils.Helpers.convertTemperature
import com.example.iti.utils.Helpers.getUnitSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesAdapter(
    private val settingsViewModel: SettingsViewModel,
    private val lifecycleScope: CoroutineScope,
    private val onItemClicked: (String) -> Unit
) : ListAdapter<WeatherEntity, FavouritesAdapter.FavouritesViewHolder>(FavouritesDiffCallback()) {

    class FavouritesDiffCallback : DiffUtil.ItemCallback<WeatherEntity>() {
        override fun areItemsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem.cityName == newItem.cityName
        }

        override fun areContentsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouritesAdapter.FavouritesViewHolder {
        val binding =
            ItemFavouriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavouritesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouritesAdapter.FavouritesViewHolder, position: Int) {
        val weatherEntity = getItem(position)
        holder.bind(weatherEntity)
    }

    inner class FavouritesViewHolder(private val binding: ItemFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("DefaultLocale")
        fun bind(weatherEntity: WeatherEntity) {
            val animation: Animation =
                AnimationUtils.loadAnimation(binding.root.context, R.anim.scale_in_animation)
            binding.root.startAnimation(animation)

            binding.root.setOnClickListener {
                onItemClicked(weatherEntity.cityName)
            }

            lifecycleScope.launch(Dispatchers.Main) {
                val unit = settingsViewModel.getTemperatureUnit()
                binding.tvTempunitLabel.text = weatherEntity.cityName

                // Ensure both arguments are passed to format string
                binding.tvTempunitValue.text = String.format(
                    "%.0f°%s",
                    convertTemperature(weatherEntity.currentTemp, unit),
                    getUnitSymbol(unit)
                )
            }
        }
    }
}