package com.example.iti.feature.settings.viewModel

import androidx.lifecycle.ViewModel
import com.example.iti.data.repository.Repository

class SettingsViewModel(private val repository: Repository) : ViewModel() {
    fun getTemperatureUnit(): String {
        return repository.getTemperatureUnit()
    }

    fun setTemperatureUnit(unit: String) {
        repository.setTemperatureUnit(unit)
    }

    fun getWindSpeedUnit(): String {
        return repository.getWindSpeedUnit()
    }

    fun setWindSpeedUnit(unit: String) {
        repository.setWindSpeedUnit(unit)
    }
    fun getNotificationPreference(): Boolean {
        return repository.getNotificationPreference()
    }

    fun setNotificationPreference(enabled: Boolean) {
        repository.setNotificationPreference(enabled)
    }

    fun getLanguage(): String {
        return repository.getLanguage()
    }

    fun setLanguage(language: String) {
        repository.setLanguage(language)
    }
}