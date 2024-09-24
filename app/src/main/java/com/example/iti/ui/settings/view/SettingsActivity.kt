package com.example.iti.ui.settings.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.iti.databinding.ActivitySettingsBinding
import com.example.iti.db.local.LocalDataSourceImpl
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.db.sharedPrefrences.SettingsDataSourceImpl
import com.example.iti.network.ApiClient
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.ui.settings.viewModel.SettingsViewModelFactory

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
                settingsDataSource = SettingsDataSourceImpl(
                    this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
                ),
                localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackButtonClick()
        onTemperatureUnitSelection()
        onWindSpeedUnitSelection()

        val savedUnit = settingsViewModel.getTemperatureUnit()
        when (savedUnit) {
            "Celsius" -> binding.radioCelsius.isChecked = true
            "Fahrenheit" -> binding.radioFahrenheit.isChecked = true
            "Kelvin" -> binding.radioKelvin.isChecked = true
        }

        val savedSpeedUnit = settingsViewModel.getWindSpeedUnit()
        when (savedSpeedUnit) {
            "Meter/Second" -> binding.radioMeterPerSecond.isChecked = true
            "Miles/Hour" -> binding.radioMilesPerHour.isChecked = true
        }
    }

    private fun onWindSpeedUnitSelection() {
        binding.radioMeterPerSecond.setOnClickListener {
            settingsViewModel.setWindSpeedUnit("Meter/Second")
        }
        binding.radioMilesPerHour.setOnClickListener {
            settingsViewModel.setWindSpeedUnit("Miles/Hour")
        }
    }


    private fun onTemperatureUnitSelection() {
        binding.radioKelvin.setOnClickListener {
            settingsViewModel.setTemperatureUnit("Kelvin")
        }
        binding.radioCelsius.setOnClickListener {
            settingsViewModel.setTemperatureUnit("Celsius")
        }
        binding.radioFahrenheit.setOnClickListener {
            settingsViewModel.setTemperatureUnit("Fahrenheit")
        }
    }

    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener { finish() }
    }

}