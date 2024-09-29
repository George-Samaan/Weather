package com.example.iti.feature.homeScreen.view

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.iti.data.db.local.favourites.LocalDataSourceImpl
import com.example.iti.data.db.remote.RemoteDataSourceImpl
import com.example.iti.data.db.room.AppDatabase
import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.data.model.DailyForecastElement
import com.example.iti.data.model.Hourly
import com.example.iti.data.model.Weather
import com.example.iti.data.model.WeatherEntity
import com.example.iti.data.network.ApiClient
import com.example.iti.data.network.ApiState
import com.example.iti.data.repository.RepositoryImpl
import com.example.iti.databinding.ActivityHomeScreenBinding
import com.example.iti.feature.alert.view.AlertActivity
import com.example.iti.feature.favourites.view.FavouritesActivity
import com.example.iti.feature.favourites.viewModel.FavouritesViewModel
import com.example.iti.feature.favourites.viewModel.FavouritesViewModelFactory
import com.example.iti.feature.googleMaps.view.GoogleMapsActivity
import com.example.iti.feature.homeScreen.viewModel.HomeViewModel
import com.example.iti.feature.homeScreen.viewModel.HomeViewModelFactory
import com.example.iti.feature.settings.view.SettingsActivity
import com.example.iti.feature.settings.viewModel.SettingsViewModel
import com.example.iti.feature.settings.viewModel.SettingsViewModelFactory
import com.example.iti.services.pushNotifications.NotificationServices.notificationServices
import com.example.iti.utils.Constants.ARABIC_SHARED
import com.example.iti.utils.Constants.FAVOURITE_SHARED_CITY
import com.example.iti.utils.Constants.HOME_SCREEN_SHARED_PREFS_NAME
import com.example.iti.utils.Constants.LATITUDE_SHARED
import com.example.iti.utils.Constants.LONGITUDE_SHARED
import com.example.iti.utils.Constants.METER_PER_SECOND
import com.example.iti.utils.Constants.OFFLINE_SHARED_PREFS_NAME
import com.example.iti.utils.Constants.SHARED_PREFS_NAME
import com.example.iti.utils.Constants.TEMPERATURE_FORMAT
import com.example.iti.utils.Helpers.convertTemperature
import com.example.iti.utils.Helpers.convertWindSpeed
import com.example.iti.utils.Helpers.date
import com.example.iti.utils.Helpers.formatTime
import com.example.iti.utils.Helpers.getUnitSymbol
import com.example.iti.utils.Helpers.getWindSpeedUnitSymbol
import com.example.iti.utils.Helpers.isNetworkAvailable
import com.example.iti.utils.HomeScreenHelper.checkWeatherDescription
import com.example.iti.utils.HomeScreenHelper.dynamicTextAnimation
import com.example.iti.utils.HomeScreenHelper.slideInAndScaleView
import com.example.iti.utils.HomeScreenHelper.slideInFromLeft
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class HomeScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter
    private var city: String = ""
    private var passedLat: Double = 0.0
    private var passedLong: Double = 0.0
    private var isViewOnly: Boolean = false
    private var cityName: String? = null

    private val weatherViewModel: HomeViewModel by viewModels { HomeViewModelFactory(getRepository()) }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            getRepository()
        )
    }
    private val favouritesViewModel: FavouritesViewModel by viewModels {
        FavouritesViewModelFactory(
            getRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkRunningLanguage()
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViews()
        setUpAdapters()
        gettingPassedKeysFromIntents()
        setCityNameBasedOnLatAndLong(passedLat, passedLong)
        fetchDataBasedOnLatAndLong()
        setUpCollector()
        savedLocationsDetails()
        swipeToRefresh()
        visibilityForViewerPage()
        if (!isNetworkAvailable(this)) {
            fetchWeatherDataFromSharedPreferences()
            showSnackBar()
        }
        notificationServices(this, passedLat, passedLong)
    }

    private fun setUpViews() {
        binding.btnMaps.setOnClickListener {
            if (isNetworkAvailable(this)) {
                startActivity(Intent(this, GoogleMapsActivity::class.java))
            } else {
                Toast.makeText(
                    this,
                    getString(com.example.iti.R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnFavourites.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
        }
        binding.btnAlert.setOnClickListener {
            startActivity(Intent(this, AlertActivity::class.java))
        }
    }

    private fun setUpAdapters() {
        dailyAdapter = DailyAdapter(settingsViewModel, lifecycleScope)
        hourlyAdapter = HourlyAdapter(settingsViewModel, lifecycleScope)

        binding.apply {
            rvHourlyDegrees.adapter = hourlyAdapter
            rvDetailedDays.adapter = dailyAdapter
        }
    }

    private fun gettingPassedKeysFromIntents() {
        passedLat = intent.getDoubleExtra(LATITUDE_SHARED, 0.0)
        Log.d("JJJJJ", "passed lat : ${passedLat}")
        passedLong = intent.getDoubleExtra(LONGITUDE_SHARED, 0.0)
        isViewOnly = intent.getBooleanExtra("viewOnly", false)
        cityName = intent.getStringExtra(FAVOURITE_SHARED_CITY)
    }

    @Suppress("DEPRECATION")
    private fun setCityNameBasedOnLatAndLong(lat: Double, long: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Safely handle potential null values for the admin area and country name
                val adminArea = address.adminArea?.takeIf { it.isNotBlank() } ?: ""
                val countryName = address.countryName?.takeIf { it.isNotBlank() } ?: ""

                // Get the language setting from SharedPreferences
                val language = settingsViewModel.getLanguage()

                // Determine the city name based on the language
                city = if (language == ARABIC_SHARED) { // Check if the language is Arabic
                    countryName // Only use the country name
                } else {
                    // Format the city string, including admin area and country name
                    listOf(adminArea, countryName)
                        .filter { it.isNotEmpty() }
                        .joinToString(", ")
                }

                if (city.isNotEmpty()) {
                    Log.e("HomeScreenActivity", "City: $city")
                }
            }
        } catch (e: IOException) {
            Log.e("HomeScreenActivity", "Geocoder service is unavailable: ${e.message}")
            // Handle the error (e.g., notify the user, use default values, etc.)
        }
    }

    private fun fetchDataBasedOnLatAndLong() {
        lifecycleScope.launch {
            try {
                weatherViewModel.fetchCurrentWeatherDataByCoordinates(passedLat, passedLong)
                weatherViewModel.fetchHourlyWeatherByCoordinates(passedLat, passedLong)
                weatherViewModel.fetchDailyWeatherByCoordinates(passedLat, passedLong)
            } catch (e: Exception) {
                Toast.makeText(
                    this@HomeScreenActivity,
                    getString(com.example.iti.R.string.no_network_using_saved_data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setUpCollector() {
        gettingWeatherDataFromViewModel()
        gettingHourlyWeatherDataFromViewModel()
        gettingDailyWeatherDataFromViewModel()
    }

    private fun saveWeatherDataToSharedPreferences(weather: Weather) {
        val sharedPreferences =
            getSharedPreferences(HOME_SCREEN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert Weather object to JSON string using Gson
        val gson = Gson()
        val weatherJson = gson.toJson(weather)

        editor.putString(OFFLINE_SHARED_PREFS_NAME, weatherJson)
        editor.apply() // Use apply() for asynchronous saving
    }

    private fun gettingWeatherDataFromViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                weatherViewModel.weatherDataStateFlow.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            showLoading(true)
                            binding.cardDaysDetails.visibility = View.GONE
                            setVisibilityOfViewsOnScreen(true)
                        }

                        is ApiState.Success -> {
                            delay(600)
                            showLoading(false)
                            slideInAndScaleView(binding.cardDaysDetails)
//                        binding.cardDaysDetails.visibility = View.VISIBLE
                            setVisibilityOfViewsOnScreen(false)
                            val weatherData = apiState.data as Weather
                            launch {
                                updateUi(weatherData) // Update UI
                            }
                            saveWeatherDataToSharedPreferences(weatherData) // Save to SharedPreferences
                        }

                        is ApiState.Failure -> {
                            showLoading(false)
                            setVisibilityOfViewsOnScreen(false)
                            binding.rvHourlyDegrees.visibility = View.GONE
                            binding.rvDetailedDays.visibility = View.GONE
                            Log.e(
                                "WeatherError",
                                "Error retrieving weather data ${apiState.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchWeatherDataFromSharedPreferences() {
        val sharedPreferences =
            getSharedPreferences(HOME_SCREEN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val weatherJson = sharedPreferences.getString(OFFLINE_SHARED_PREFS_NAME, null)

        if (weatherJson != null) {
            // Convert JSON string back to Weather object
            val gson = Gson()
            val weatherData: Weather = gson.fromJson(weatherJson, Weather::class.java)
            updateUi(weatherData) // Update UI with the saved weather data
            Log.d("mahmoud", "Weather data loaded from SharedPreferences ${weatherData}")
        } else {
            // Handle case when no data is saved
            Log.e("WeatherError", "No weather data available in SharedPreferences.")
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale", "StringFormatMatches")
    private fun updateUi(weather: Weather) {
        val unit = settingsViewModel.getTemperatureUnit()
        val windSpeedUnit = settingsViewModel.getWindSpeedUnit()

        //set Lottie based on weather
        val lottieAnimation = checkWeatherDescription(this, weather)
        binding.animWeather.setAnimation(lottieAnimation)
        binding.animWeather.playAnimation()

        //update Temp
        val currentTemp = convertTemperature(weather.main.temp, unit)
        binding.tvCurrentDegree.text =
            String.format(TEMPERATURE_FORMAT, currentTemp, getUnitSymbol(unit))
        val minTemp = convertTemperature(weather.main.temp_min, unit)
        binding.tvTempMin.text = String.format(TEMPERATURE_FORMAT, minTemp, getUnitSymbol(unit))
        val maxTemp = convertTemperature(weather.main.temp_max, unit)
        binding.tvTempMax.text = String.format(TEMPERATURE_FORMAT, maxTemp, getUnitSymbol(unit))

        //update weather details
        val cityName = city.trim()
        val words = cityName.split(" ")

        if (words.size > 2) {
            val firstLine = words.take(2).joinToString(" ") // First two words
            val secondLine = words.drop(2).joinToString(" ") // Remaining words
            binding.tvCityName.text = "$firstLine\n$secondLine"
        } else {
            binding.tvCityName.text = cityName // If it's 2 words or less, keep it as is
        }
        binding.tvWeatherStatus.text = weather.weather[0].description
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        binding.tvDate.text = date()
        binding.tvPressureValue.text =
            getString(com.example.iti.R.string.hpa, weather.main.pressure)
        binding.tvHumidityValue.text = "${weather.main.humidity} %"
        val windSpeed = convertWindSpeed(weather.wind.speed, METER_PER_SECOND, windSpeedUnit)
        binding.tvWindValue.text = String.format(
            Locale.getDefault(),
            "%.0f %s",
            windSpeed,
            getString(getWindSpeedUnitSymbol(windSpeedUnit))
        )

        // additional info
        binding.tvCloudValue.text = "${weather.clouds.all} %"
        binding.tvSunriseValue.text = formatTime(weather.sys.sunrise)
        binding.tvSunsetValue.text = formatTime(weather.sys.sunset)


        // Save data to local database when Save button is clicked
        onSaveButtonClick(
            currentTemp,
            minTemp,
            maxTemp,
            weather,
            windSpeed,
            passedLat,
            passedLong,
            lottieAnimation
        )
    }

    private fun onSaveButtonClick(
        currentTemp: Double,
        minTemp: Double,
        maxTemp: Double,
        weather: Weather,
        windSpeed: Double,
        lat: Double,
        long: Double,
        lottie: Int
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
                date = binding.tvDate.text.toString(),
                latitude = lat,
                longitude = long,
                lottie = lottie
            )
            // Insert the WeatherEntity into the database via the ViewModel
            lifecycleScope.launch {
                favouritesViewModel.insertWeatherData(weatherEntity)
                Toast.makeText(
                    this@HomeScreenActivity,
                    getString(com.example.iti.R.string.location_saved_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun gettingHourlyWeatherDataFromViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                weatherViewModel.hourlyForecastDataStateFlow.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                        }

                        is ApiState.Success -> {
                            val hourlyList = (apiState.data as Hourly).list.take(9)
                            hourlyAdapter.submitList(hourlyList)
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
        }
    }

    private fun gettingDailyWeatherDataFromViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                weatherViewModel.dailyForecastDataStateFlow.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.rvDetailedDays.visibility = View.GONE
                            binding.cardDaysDetails.visibility = View.GONE
                        }

                        is ApiState.Success -> {
                            delay(600)
                            binding.rvDetailedDays.visibility = View.VISIBLE
                            binding.cardDaysDetails.visibility = View.VISIBLE
                            val dailyList =
                                (apiState.data as List<*>).filterIsInstance<DailyForecastElement>()
                            dailyAdapter.submitList(dailyList)
                        }

                        is ApiState.Failure -> {
                            binding.rvDetailedDays.visibility = View.GONE
                            binding.cardDaysDetails.visibility = View.GONE
                            Log.e(
                                "WeatherError",
                                "Error retrieving daily forecast data ${apiState.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun savedLocationsDetails() {
        if (cityName != null) {
            lifecycleScope.launch {
                if (isNetworkAvailable(this@HomeScreenActivity)) {
                    val weatherEntity = favouritesViewModel.getWeatherCity(cityName!!)
                    if (weatherEntity != null) {
                        disableViewsForFavouritesViewer()
                        fetchDataFromDataBaseOrFromRemoteIfNetworkAvailable(weatherEntity)
                    }
                } else {
                    val weatherEntity = favouritesViewModel.getWeatherCity(cityName!!)
                    if (weatherEntity != null) {
                        // Load the offline weather data (you might want to update UI accordingly)
                        loadOfflineData(weatherEntity)
                        Log.e(
                            "HomeScreenActivity",
                            "Loaded offline data for ${weatherEntity.cityName}"
                        )
                    }
                }
            }
        }
    }

    private fun disableViewsForFavouritesViewer() {
        binding.btnMaps.visibility = View.GONE
        binding.btnFavourites.visibility = View.GONE
        binding.btnAlert.visibility = View.GONE
        binding.btnSave.visibility = View.GONE
        binding.btnSettings.visibility = View.GONE
    }

    private fun fetchDataFromDataBaseOrFromRemoteIfNetworkAvailable(weatherEntity: WeatherEntity) {
        weatherViewModel.fetchCurrentWeatherDataByCoordinates(
            weatherEntity.latitude,
            weatherEntity.longitude
        )
        setCityNameBasedOnLatAndLong(weatherEntity.latitude, weatherEntity.longitude)
        binding.swipeToRefresh.setOnRefreshListener {
            weatherViewModel.fetchCurrentWeatherDataByCoordinates(
                weatherEntity.latitude,
                weatherEntity.longitude
            )
            binding.swipeToRefresh.isRefreshing = false
        }
        Log.e("HomeScreenActivity", "Loaded data from database")
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun loadOfflineData(weatherEntity: WeatherEntity) {
        // Retrieve units from the SettingsViewModel
        val unit = settingsViewModel.getTemperatureUnit()
        val windSpeedUnit = settingsViewModel.getWindSpeedUnit()

        // Convert and display temperatures
        val currentTemp = convertTemperature(weatherEntity.currentTemp, unit)
        binding.tvCurrentDegree.text =
            String.format(TEMPERATURE_FORMAT, currentTemp, getUnitSymbol(unit))
        val minTemp = convertTemperature(weatherEntity.minTemp, unit)
        binding.tvTempMin.text = String.format(TEMPERATURE_FORMAT, minTemp, getUnitSymbol(unit))
        val maxTemp = convertTemperature(weatherEntity.maxTemp, unit)
        binding.tvTempMax.text = String.format(TEMPERATURE_FORMAT, maxTemp, getUnitSymbol(unit))

        // Set other weather details
        binding.tvCityName.text = weatherEntity.cityName
        binding.tvWeatherStatus.text = weatherEntity.description
        binding.tvDate.text = weatherEntity.date
        binding.tvPressureValue.text = "${weatherEntity.pressure} hpa"
        binding.tvHumidityValue.text = "${weatherEntity.humidity} %"

        // Convert and display wind speed
        val windSpeed = convertWindSpeed(weatherEntity.windSpeed, METER_PER_SECOND, windSpeedUnit)
        binding.tvWindValue.text = String.format(
            Locale.getDefault(),
            "%.0f %s",
            windSpeed,
            getString(getWindSpeedUnitSymbol(windSpeedUnit))
        )

        // Additional weather info
        binding.tvCloudValue.text = "${weatherEntity.clouds} %"
        binding.tvSunriseValue.text = formatTime(weatherEntity.sunrise)
        binding.tvSunsetValue.text = formatTime(weatherEntity.sunset)

        // Set Lottie animation
        binding.animWeather.setAnimation(weatherEntity.lottie)
        binding.animWeather.playAnimation()

        // Hide buttons and disable swipe-to-refresh
        disableViewsForFavouritesViewer()
        binding.swipeToRefresh.isActivated = false
    }

    private fun swipeToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            if (isNetworkAvailable(this)) {
                fetchDataBasedOnLatAndLong()
                binding.swipeToRefresh.isRefreshing = false
            } else {
                showSnackBar()
                binding.swipeToRefresh.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDataBasedOnLatAndLong()
        setUpAdapters()
        if (!isNetworkAvailable(this)) {
            showSnackBar()
        }
    }

    private fun getRepository() = RepositoryImpl(
        remoteDataSource = RemoteDataSourceImpl(
            apiService = ApiClient.retrofit,
            sharedPrefsDataSource = SharedPrefsDataSourceImpl(
                this.getSharedPreferences(
                    SHARED_PREFS_NAME,
                    MODE_PRIVATE
                )
            )
        ),
        sharedPrefsDataSource = SharedPrefsDataSourceImpl(
            this.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        ),
        localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
    )

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
            binding.tvDate.visibility = View.GONE
        } else {
            slideInFromLeft(binding.tvCityName)
            slideInFromLeft(binding.tvDate)
            dynamicTextAnimation(binding.tvCurrentDegree)
            slideInAndScaleView(binding.tvWeatherStatus)
            dynamicTextAnimation(binding.tvTempMin)
            dynamicTextAnimation(binding.tvTempMax)
            slideInAndScaleView(binding.cardWeatherDetails)
            slideInAndScaleView(binding.rvHourlyDegrees)
            slideInAndScaleView(binding.rvDetailedDays)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun visibilityForViewerPage() {
        if (isViewOnly) {
            binding.btnMaps.visibility = View.GONE
            binding.btnFavourites.visibility = View.GONE
            binding.btnAlert.visibility = View.GONE
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    private fun showSnackBar() {
        val snackBar = Snackbar.make(
            findViewById(R.id.content),
            getString(com.example.iti.R.string.no_network_connection_data_loaded_from_cache),
            Snackbar.LENGTH_LONG
        )
        snackBar.show()
    }

    private fun checkRunningLanguage() {
        val language = settingsViewModel.getLanguage()
        val locale = Locale(language)
        Log.e("lang", "curren lang $language")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        applicationContext.createConfigurationContext(config)
        applicationContext.resources.updateConfiguration(config, resources.displayMetrics)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}