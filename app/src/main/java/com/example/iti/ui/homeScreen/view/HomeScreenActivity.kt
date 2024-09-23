package com.example.iti.ui.homeScreen.view

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iti.R
import com.example.iti.databinding.ActivityHomeScreenBinding
import com.example.iti.db.local.LocalDataSourceImpl
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.db.sharedPrefrences.SettingsDataSourceImpl
import com.example.iti.model.DailyForecastElement
import com.example.iti.model.Hourly
import com.example.iti.model.Weather
import com.example.iti.model.WeatherEntity
import com.example.iti.network.ApiClient
import com.example.iti.network.ApiState
import com.example.iti.ui.favourites.view.FavouritesActivity
import com.example.iti.ui.favourites.viewModel.FavouritesViewModel
import com.example.iti.ui.favourites.viewModel.FavouritesViewModelFactory
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
    private var isViewOnly: Boolean = false

    private val weatherViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
                settingsDataSource = SettingsDataSourceImpl(
                    this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
                ),
                localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
            )
        )
    }

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

    private val favouritesViewModel: FavouritesViewModel by viewModels {
        FavouritesViewModelFactory(
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
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // setup views & adapters & observers
        setUpViews()
        setUpAdapters()

        gettingLatAndLongFromMaps()
        setCityNameBasedOnLatAndLong()

        fetchDataBasedOnLatAndLong()
        setUpCollector()

        swipeToRefresh()

        visibilityForViewerPage()
    }

    private fun visibilityForViewerPage() {
        if (isViewOnly) {
            binding.btnMaps.visibility = View.GONE
            binding.btnFavourites.visibility = View.GONE
            binding.btnHomeNotification.visibility = View.GONE
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    //check it later
    private fun swipeToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            fetchDataBasedOnLatAndLong()
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setVisibilityOfViewsOnScreen(isLoading: Boolean) {
        if (isLoading) {
            binding.tvCityName.visibility = View.GONE
            binding.tvCurrentDegree.visibility = View.GONE
            binding.tvWeatherStatus.visibility = View.GONE
            binding.tvTempMin.visibility = View.GONE
            binding.tvTempMax.visibility = View.GONE
            binding.cardWeatherDetails.visibility = View.GONE
            binding.rvHourlyDegrees.visibility = View.GONE
            binding.rvDetailedDays.visibility = View.GONE
        } else {
            slideInAndScaleView(binding.tvCityName)
            slideInAndScaleView(binding.tvCurrentDegree)
            slideInAndScaleView(binding.tvWeatherStatus)
            slideInAndScaleView(binding.tvTempMin)
            slideInAndScaleView(binding.tvTempMax)
            slideInAndScaleView(binding.cardWeatherDetails)
            slideInAndScaleView(binding.rvHourlyDegrees)
            slideInAndScaleView(binding.rvDetailedDays)
        }
    }

    private fun slideInAndScaleView(view: View) {
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

    private fun setUpAdapters() {
        dailyAdapter = DailyAdapter(settingsViewModel, lifecycleScope)
        binding.rvDetailedDays.adapter = dailyAdapter

        adapter = HourlyAdapter(settingsViewModel, lifecycleScope)
        binding.rvHourlyDegrees.adapter = adapter
    }

    private fun setUpCollector() {
        lifecycleScope.launch {
            weatherViewModel.weatherDataStateFlow.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                        showLoading(true)
                        setVisibilityOfViewsOnScreen(true)
                        binding.rvHourlyDegrees.visibility = View.GONE
                        binding.cardDaysDetails.visibility = View.GONE
                    }

                    is ApiState.Success -> {
                        showLoading(false)
                        setVisibilityOfViewsOnScreen(false)
                        binding.rvHourlyDegrees.visibility = View.VISIBLE
                        binding.cardDaysDetails.visibility = View.VISIBLE
                        updateUi(apiState.data as Weather)
                    }

                    is ApiState.Failure -> {
                        showLoading(false)
                        setVisibilityOfViewsOnScreen(false)
                        binding.rvHourlyDegrees.visibility = View.GONE
                        binding.rvDetailedDays.visibility = View.GONE

                        Log.e("WeatherError", "Error retrieving weather data ${apiState.message}")
                    }
                }
            }
        }

        lifecycleScope.launch {
            weatherViewModel.hourlyForecastDataStateFlow.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        val hourlyList = (apiState.data as Hourly).list.take(9)
                        adapter.submitList(hourlyList)
                    }

                    is ApiState.Failure -> {
                        Log.e(
                            "WeatherError",
                            "Error retrieving hourly forecast data ${apiState.message}"
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            weatherViewModel.dailyForecastDataStateFlow.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        val dailyList =
                            (apiState.data as List<*>).filterIsInstance<DailyForecastElement>()
                        dailyAdapter.submitList(dailyList)
                    }

                    is ApiState.Failure -> {
                        Log.e(
                            "WeatherError",
                            "Error retrieving daily forecast data ${apiState.message}"
                        )
                    }
                }
            }
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

        //set Lottie based on weather
        val lottieAnimation = checkWeatherDescription(weather)
        binding.animWeather.setAnimation(lottieAnimation)
        binding.animWeather.playAnimation()

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
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
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

        // Save data to local database when Save button is clicked
        onSaveButtonClick(currentTemp, minTemp, maxTemp, weather, windSpeed)
    }

    private fun onSaveButtonClick(
        currentTemp: Double,
        minTemp: Double,
        maxTemp: Double,
        weather: Weather,
        windSpeed: Double
    ) {
        binding.btnSave.setOnClickListener {
            // Create a WeatherEntity from the current UI data
            val weatherEntity = WeatherEntity(
                cityName = city,
                description = binding.tvWeatherStatus.text.toString(),
                currentTemp = currentTemp,
                minTemp = minTemp,
                maxTemp = maxTemp,
                pressure = weather.main.pressure,
                humidity = weather.main.humidity,
                windSpeed = windSpeed,
                clouds = weather.clouds.all.toInt(),
                sunrise = weather.sys.sunrise,
                sunset = weather.sys.sunset,
                date = binding.tvDate.text.toString()
            )
            // Insert the WeatherEntity into the database via the ViewModel
            lifecycleScope.launch {
                favouritesViewModel.insertWeatherData(weatherEntity)
                Toast.makeText(
                    this@HomeScreenActivity,
                    "Location saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun checkWeatherDescription(weather: Weather): Int {
        val lottieAnimation = when (weather.weather[0].description.lowercase()) {
            "clear sky" -> R.raw.clear_sky_anim
            "few clouds" -> R.raw.few_clouds
            "scattered clouds" -> R.raw.scattered_clouds_anim
            "broken clouds" -> R.raw.broken_cloud_anim
            "overcast clouds" -> R.raw.overcast_clouds_anim
            "light intensity shower rain" -> R.raw.rain_anim
            "light rain" -> R.raw.rain_anim
            "moderate rain" -> R.raw.rain_anim
            "light snow" -> R.raw.snow_anim
            //underTesting
            "thunderstorm" -> R.raw.thunderstorm
            "mist" -> R.raw.mist
            else -> R.raw.clear_sky_anim
        }
        return lottieAnimation
    }

    @Suppress("DEPRECATION")
    private fun setCityNameBasedOnLatAndLong() {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(passedLat, passedLong, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]

            // Safely handle potential null values for the admin area and country name
            val adminArea = address.adminArea?.takeIf { it.isNotBlank() } ?: ""
            val countryName = address.countryName?.takeIf { it.isNotBlank() } ?: ""

            // Format the city string, omitting null or blank values
            city = listOf(adminArea, countryName)
                .filter { it.isNotEmpty() }
                .joinToString(", ")

            if (city.isNotEmpty()) {
                Log.e("HomeScreenActivity", "City: $city")
            }
        }
    }

    private fun gettingLatAndLongFromMaps() {
        passedLat = intent.getDoubleExtra("latitude", 0.0)
        passedLong = intent.getDoubleExtra("longitude", 0.0)
        isViewOnly = intent.getBooleanExtra("viewOnly", false)
        Log.e("HomeScreenActivity", "Latitude: $passedLat, Longitude: $passedLong")
    }

    private fun setUpViews() {
        binding.btnMaps.setOnClickListener {
            startActivity(Intent(this, GoogleMapsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnFavourites.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDataBasedOnLatAndLong()
        setUpAdapters()
    }
}