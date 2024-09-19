package com.example.iti.ui.homeScreen.view

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iti.databinding.ActivityHomeBinding
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.model.Weather
import com.example.iti.ui.googleMaps.GoogleMapsActivity
import com.example.iti.ui.homeScreen.viewModel.HomeViewModel
import com.example.iti.ui.homeScreen.viewModel.HomeViewModelFactory
import com.example.iti.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeScreenActivity : AppCompatActivity() {
    private var city: String = ""
    private var passedLat: Double = 0.0
    private var passedLong: Double = 0.0

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private val weatherViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            RepositoryImpl(remoteDataSource = RemoteDataSourceImpl())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpViews()
        setUpObserver()
        gettingLatAndLongFromMaps()
        setCityNameBasedOnLatAndLong()
        fetchDataBasedOnLatAndLong()

    }

    private fun fetchDataBasedOnLatAndLong() {
        lifecycleScope.launch {
            weatherViewModel.fetchWeatherByCoordinates(passedLat, passedLong)
        }
    }

    private fun setCityNameBasedOnLatAndLong() {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(passedLat, passedLong, 1)
        if (addresses != null && addresses.isNotEmpty()) {
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

    private fun setUpObserver() {
        weatherViewModel.weatherDataByCoordinates.observe(this) { result ->
            result.fold(
                onSuccess = { cordinates ->
                    updateUi(cordinates)
                },
                onFailure = {
                    Log.d(
                        "WeatherRepository",
                        "Error retrieving weather data by coordinates: ${it.message}"
                    )
                }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(weather: Weather) {
        Log.d("WeatherResponse", "Weather data: $weather")
        val currentDegree = weather.main.temp.toInt()
        binding.tvCurrentDegree.text = "${currentDegree}°C"

        val minTemp = weather.main.temp_min.toInt()
        binding.tvTempMin.text = "${minTemp}°C"

        val maxTemp = weather.main.temp_max.toInt()
        binding.tvTempMax.text = "${maxTemp}°C"
        Log.d("jeoo", "maxtemp ${maxTemp}")

        binding.tvCityName.text = city

        binding.tvWeatherStatus.text = weather.weather[0].description

        binding.tvDate.text = date()

        binding.tvPressureValue.text = "${weather.main.pressure} hpa"
        binding.tvHumidityValue.text = "${weather.main.humidity} %"
        binding.tvWindValue.text = "${weather.wind.speed} m/s"
        binding.tvCloudValue.text = "${weather.clouds.all} %"

        val sunrise = weather.sys.sunrise
        binding.tvSunsetValue.text = time(sunrise)

        val sunset = weather.sys.sunset
        binding.tvSunriseValue.text = time(sunset)

    }

    @SuppressLint("SimpleDateFormat")
    private fun date(): String {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    private fun time(timesTemp: Long): String {
        val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return simpleDateFormat.format(Date(timesTemp * 1000))
    }

    private fun setUpViews() {
        binding.btnMaps.setOnClickListener {
            val intent = Intent(this, GoogleMapsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}