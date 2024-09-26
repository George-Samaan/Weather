package com.example.iti.ui.settings.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.example.iti.databinding.ActivitySettingsBinding
import com.example.iti.db.local.LocalDataSourceImpl
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.network.ApiClient
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.ui.settings.viewModel.SettingsViewModelFactory

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
                sharedPrefsDataSource = SharedPrefsDataSourceImpl(
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

        onTurnOnNotificationsClick()
        onTurnOffNotificationsClick()

        val notificationsEnabled = settingsViewModel.getNotificationPreference()
        when (notificationsEnabled) {
            true -> binding.radioOnNotification.isChecked = true
            false -> binding.radioOffNotification.isChecked = true
        }
    }

    private fun onTurnOffNotificationsClick() {
        binding.radioOffNotification.setOnClickListener {
            handleNotificationPreference(false)
        }
    }

    private fun onTurnOnNotificationsClick() {
        binding.radioOnNotification.setOnClickListener {
            handleNotificationPreference(true)
        }
    }

    private fun handleNotificationPreference(enable: Boolean) {
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
        } else {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, this.packageName)
            startActivity(intent)
        }
        settingsViewModel.setNotificationPreference(enable)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("TAG", "Permission Granted")
                handleNotificationPreference(true)
            } else {
                Log.e("TAG", "Permission Denied")
                handleNotificationPreference(false)
            }
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

    override fun onResume() {
        super.onResume()
        val areSystemNotificationsEnabled =
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        if (areSystemNotificationsEnabled) {
            binding.radioOnNotification.isChecked = true
            settingsViewModel.setNotificationPreference(true)
        } else {
            binding.radioOffNotification.isChecked = true
            settingsViewModel.setNotificationPreference(false)
        }
    }
}