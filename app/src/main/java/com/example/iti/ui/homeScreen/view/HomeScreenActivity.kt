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
import com.example.iti.utils.Helpers.isNetworkAvailable
import com.example.iti.utils.HomeScreenHelper.checkWeatherDescription
import com.example.iti.utils.HomeScreenHelper.slideInAndScaleView
import kotlinx.coroutines.launch
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
        setHomeScreenFromMap()
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
    private fun setUpAdapters() {
        dailyAdapter = DailyAdapter(settingsViewModel, lifecycleScope)
        hourlyAdapter = HourlyAdapter(settingsViewModel, lifecycleScope)

        binding.apply {
            rvHourlyDegrees.adapter = hourlyAdapter
            rvDetailedDays.adapter = dailyAdapter
        }
    }

    private fun gettingPassedKeysFromIntents() {
        passedLat = intent.getDoubleExtra("latitude", 0.0)
        passedLong = intent.getDoubleExtra("longitude", 0.0)
        isViewOnly = intent.getBooleanExtra("viewOnly", false)
        cityName = intent.getStringExtra("CITY_KEY")
    }

    @Suppress("DEPRECATION")
    private fun setCityNameBasedOnLatAndLong(lat: Double, long: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, long, 1)
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

    private fun fetchDataBasedOnLatAndLong() {
        lifecycleScope.launch {
            try {
                weatherViewModel.fetchCurrentWeatherDataByCoordinates(passedLat, passedLong)
                weatherViewModel.fetchHourlyWeatherByCoordinates(passedLat, passedLong)
                weatherViewModel.fetchDailyWeatherByCoordinates(passedLat, passedLong)
            } catch (e: Exception) {
                Toast.makeText(
                    this@HomeScreenActivity,
                    "No network. Using saved data.",
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

    private fun gettingWeatherDataFromViewModel() {
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
                    "Location saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun gettingHourlyWeatherDataFromViewModel() {
        lifecycleScope.launch {
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

    private fun gettingDailyWeatherDataFromViewModel() {
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

    private fun savedLocationsDetails() {
        if (cityName != null) {
            lifecycleScope.launch {
                if (isNetworkAvailable(this@HomeScreenActivity)) {
                    val weatherEntity = favouritesViewModel.getWeatherCity(cityName!!)
                    if (weatherEntity != null) {
                        disableViewsForFavouritesViewer()
                        fetchDataOnlineFromDataBaseIfNetworkAvailable(weatherEntity)
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
        binding.btnHomeNotification.visibility = View.GONE
        binding.btnSave.visibility = View.GONE
        binding.btnSettings.visibility = View.GONE
    }

    private fun fetchDataOnlineFromDataBaseIfNetworkAvailable(weatherEntity: WeatherEntity) {
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
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun loadOfflineData(weatherEntity: WeatherEntity) {
        // Retrieve units from the SettingsViewModel
        val unit = settingsViewModel.getTemperatureUnit()
        val windSpeedUnit = settingsViewModel.getWindSpeedUnit()

        // Convert and display temperatures
        val currentTemp = convertTemperature(weatherEntity.currentTemp, unit)
        binding.tvCurrentDegree.text = String.format("%.0f°%s", currentTemp, getUnitSymbol(unit))

        val minTemp = convertTemperature(weatherEntity.minTemp, unit)
        binding.tvTempMin.text = String.format("%.0f°%s", minTemp, getUnitSymbol(unit))

        val maxTemp = convertTemperature(weatherEntity.maxTemp, unit)
        binding.tvTempMax.text = String.format("%.0f°%s", maxTemp, getUnitSymbol(unit))

        // Set other weather details
        binding.tvCityName.text = weatherEntity.cityName
        binding.tvWeatherStatus.text = weatherEntity.description
        binding.tvDate.text = weatherEntity.date
        binding.tvPressureValue.text = "${weatherEntity.pressure} hpa"
        binding.tvHumidityValue.text = "${weatherEntity.humidity} %"

        // Convert and display wind speed
        val windSpeed = convertWindSpeed(weatherEntity.windSpeed, "Meter/Second", windSpeedUnit)
        binding.tvWindValue.text = String.format(
            Locale.getDefault(),
            "%.0f %s",
            windSpeed,
            getWindSpeedUnitSymbol(windSpeedUnit)
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
            fetchDataBasedOnLatAndLong()
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun visibilityForViewerPage() {
        if (isViewOnly) {
            binding.btnMaps.visibility = View.GONE
            binding.btnFavourites.visibility = View.GONE
            binding.btnHomeNotification.visibility = View.GONE
            binding.btnSave.visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        fetchDataBasedOnLatAndLong()
        setUpAdapters()
    }
    private fun getRepository() = RepositoryImpl(
        remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
        settingsDataSource = SettingsDataSourceImpl(
            this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
        ),
        localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
    )

    private fun setHomeScreenFromMap() {
        lifecycleScope.launch {
            weatherViewModel.fetchCurrentWeatherDataByCoordinates(passedLat, passedLong)
        }
    }
}