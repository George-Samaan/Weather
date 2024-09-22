package com.example.iti.ui.homeScreen.view

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iti.databinding.ActivityHomeScreenBinding
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.sharedPrefrences.SettingsDataSourceImpl
import com.example.iti.model.Weather
import com.example.iti.ui.googleMaps.GoogleMapsActivity
import com.example.iti.ui.homeScreen.viewModel.HomeViewModel
import com.example.iti.ui.homeScreen.viewModel.HomeViewModelFactory
import com.example.iti.ui.settings.view.SettingsActivity
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.ui.settings.viewModel.SettingsViewModelFactory
import com.example.iti.utils.Helpers.convertTemperature
import com.example.iti.utils.Helpers.convertWindSpeed
import com.example.iti.utils.Helpers.date
import com.example.iti.utils.Helpers.formatTime
import com.example.iti.utils.Helpers.getUnitSymbol
import com.example.iti.utils.Helpers.getWindSpeedUnitSymbol
import kotlinx.coroutines.launch
import java.util.Locale

class HomeScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var adapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter
    private var city: String = ""
    private var passedLat: Double = 0.0
    private var passedLong: Double = 0.0

    private val weatherViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(),
                settingsDataSource = SettingsDataSourceImpl(
                    this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
                )
            )
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(),
                settingsDataSource = SettingsDataSourceImpl(
                    this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // setup views & adapters & observers
        setUpViews()
        setUpAdapters()
        setUpObserver()

        gettingLatAndLongFromMaps()

        setCityNameBasedOnLatAndLong()
        fetchDataBasedOnLatAndLong()

    }

    private fun setUpAdapters() {
        dailyAdapter = DailyAdapter()
        binding.rvDetailedDays.adapter = dailyAdapter

        adapter = HourlyAdapter()
        binding.rvHourlyDegrees.adapter = adapter
    }

    private fun setUpObserver() {
        weatherViewModel.weatherDataByCoordinates.observe(this) { result ->
            result.fold(
                onSuccess = { weather -> updateUi(weather) },
                onFailure = { error -> Log.e("WeatherError", "Error: ${error.message}") }

            )
        }
        weatherViewModel.hourlyForecastDataByCoordinates.observe(this) { result ->
            result.fold(onSuccess = { hourlyForecast ->
                val limitedHourList = hourlyForecast.list.take(9)
                adapter.submitList(limitedHourList)
            },
                onFailure = { Log.e("WeatherError", "Error retrieving hourly data") }
            )
        }
        weatherViewModel.dailyForecastDataByCoordinates.observe(this) { dailyForecastList ->
            dailyAdapter.submitList(dailyForecastList)
        }
    }

    private fun fetchDataBasedOnLatAndLong() {
        lifecycleScope.launch {
            weatherViewModel.fetchCurrentWeatherDataByCoordinates(passedLat, passedLong)
            weatherViewModel.fetchHourlyWeatherByCoordinates(passedLat, passedLong)
            weatherViewModel.fetchDailyWeatherByCoordinates(passedLat, passedLong)
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun updateUi(weather: Weather) {
        val unit = settingsViewModel.getTemperatureUnit()
        val windSpeedUnit = settingsViewModel.getWindSpeedUnit()

        //update Temp
        val currentTemp = convertTemperature(weather.main.temp, unit)
        binding.tvCurrentDegree.text = String.format("%.0f°%s", currentTemp, getUnitSymbol(unit))
        val minTemp = convertTemperature(weather.main.temp_min, unit)
        binding.tvTempMin.text = String.format("%.0f°%s", minTemp, getUnitSymbol(unit))
        val maxTemp = convertTemperature(weather.main.temp_max, unit)
        binding.tvTempMax.text = String.format("%.0f°%s", maxTemp, getUnitSymbol(unit))

        //update weather details
        binding.tvCityName.text = city
        binding.tvWeatherStatus.text = weather.weather[0].description
        binding.tvDate.text = date()
        binding.tvPressureValue.text = "${weather.main.pressure} hpa"
        binding.tvHumidityValue.text = "${weather.main.humidity} %"
        val windSpeed = convertWindSpeed(weather.wind.speed, "Meter/Second", windSpeedUnit)
        binding.tvWindValue.text = String.format(
            Locale.getDefault(),
            "%.0f %s",
            windSpeed,
            getWindSpeedUnitSymbol(windSpeedUnit)
        )

        // additional info
        binding.tvCloudValue.text = "${weather.clouds.all} %"
        binding.tvSunriseValue.text = formatTime(weather.sys.sunrise)
        binding.tvSunsetValue.text = formatTime(weather.sys.sunset)
    }

    @Suppress("DEPRECATION")
    private fun setCityNameBasedOnLatAndLong() {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(passedLat, passedLong, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            city = "${address.adminArea}, ${address.countryName}"
            Log.e("HomeScreenActivity", "City: $city")
        }
    }

    private fun gettingLatAndLongFromMaps() {
        passedLat = intent.getDoubleExtra("latitude", 0.0)
        passedLong = intent.getDoubleExtra("longitude", 0.0)
        Log.e("HomeScreenActivity", "Latitude: $passedLat, Longitude: $passedLong")
    }

    private fun setUpViews() {
        binding.btnMaps.setOnClickListener {
            startActivity(Intent(this, GoogleMapsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDataBasedOnLatAndLong()
        setUpAdapters()
    }
}