package com.example.iti.ui.alert.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iti.R
import com.example.iti.databinding.ItemAlertBinding
import com.example.iti.model.AlarmEntity
import com.example.iti.utils.Helpers.formatDatePlusYears

class AlertAdapter() :
    ListAdapter<AlarmEntity, AlertAdapter.AlertViewHolder>(FavouritesDiffCallback()) {

    class FavouritesDiffCallback : DiffUtil.ItemCallback<AlarmEntity>() {
        override fun areItemsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity): Boolean {
            return oldItem.timeInMillis == newItem.timeInMillis
        }

        override fun areContentsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlertAdapter.AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertAdapter.AlertViewHolder, position: Int) {
        val alarmEntity = getItem(position)
        holder.bind(alarmEntity)
    }

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("DefaultLocale")
        fun bind(alarmEntity: AlarmEntity) {
            val animation: Animation =
                AnimationUtils.loadAnimation(binding.root.context, R.anim.scale_in_animation)
            binding.root.startAnimation(animation)
            binding.dateTimeAlarm.text = formatDatePlusYears(alarmEntity.timeInMillis)
        }
    }
}