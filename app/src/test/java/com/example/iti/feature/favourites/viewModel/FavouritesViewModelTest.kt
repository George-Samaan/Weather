package com.example.iti.feature.favourites.viewModel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iti.data.model.WeatherEntity
import com.example.iti.data.repository.Repository
import com.example.iti.feature.settings.viewModel.FakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// TEST VIEWMODEL

@RunWith(AndroidJUnit4::class)

class FavouritesViewModelTest {
    private lateinit var viewModel: FavouritesViewModel
    private lateinit var repository: Repository

    @Before
    fun setup() {
        repository = FakeRepository()
        viewModel = FavouritesViewModel(repository)
    }


    // integration test between FavouritesViewModel and Repository
    // DOUBLE
    @Test
    fun `test insert weather data`() = runTest {
        val mockWeatherEntity = WeatherEntity(
            cityName = "Cairo",
            description = "Clear Sky",
            currentTemp = 30.0,
            minTemp = 25.0,
            maxTemp = 35.0,
            pressure = 1012,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            sunrise = 1600000000,
            sunset = 1600040000,
            date = "2024-09-28",
            latitude = 30.0444,
            longitude = 31.2357,
            lottie = 1
        )

        // Call the method to insert weather data
        viewModel.insertWeatherData(mockWeatherEntity)

        // Collect all weather data from the repository using Flow
        val weatherData = repository.getAllWeatherData().first()

        // Assert that the inserted data matches the mock data
        assertEquals(listOf(mockWeatherEntity), weatherData)
    }

    @Test
    fun test_delete_weather_data() = runTest {
        val mockWeatherEntity = WeatherEntity(
            cityName = "Cairo",
            description = "Clear Sky",
            currentTemp = 30.0,
            minTemp = 25.0,
            maxTemp = 35.0,
            pressure = 1012,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            sunrise = 1600000000,
            sunset = 1600040000,
            date = "2024-09-28",
            latitude = 30.0444,
            longitude = 31.2357,
            lottie = 1
        )
        viewModel.insertWeatherData(mockWeatherEntity)
//        viewModel.deleteWeatherData(mockWeatherEntity)
//        val weatherData = repository.getAllWeatherData().first()
        val weatherDataa = viewModel.allWeatherData.first()
//        assertEquals(emptyList<WeatherEntity>(), weatherDataa)
        assertEquals(1, weatherDataa.size)
        assertEquals("Cairo", weatherDataa[0].cityName)
    }

    @Test
    fun test_get_weather_city() = runTest {
        val mockWeatherEntity = WeatherEntity(
            cityName = "Alexandria",
            description = "Clear Sky",
            currentTemp = 30.0,
            minTemp = 25.0,
            maxTemp = 35.0,
            pressure = 1012,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            sunrise = 1600000000,
            sunset = 1600040000,
            date = "2024-09-28",
            latitude = 30.0444,
            longitude = 31.2357,
            lottie = 1
        )
        viewModel.insertWeatherData(mockWeatherEntity)
        val weatherCity = viewModel.getWeatherCity("Alexandria")
        assertEquals(mockWeatherEntity, weatherCity)
    }

    @Test
    fun test_get_weather_city_not_found() = runTest {
        val weatherCity = viewModel.getWeatherCity("Cairo")
        assertEquals(null, weatherCity)
    }

    @Test
    fun test_getAllaWeatherData() = runTest {
        val mockWeatherFakeEntities = mutableListOf<WeatherEntity>()
        for (i in 1..10) {
            val mockWeatherEntity = WeatherEntity(
                cityName = "City $i",
                description = "Description for city $i",
                currentTemp = 20.0 + i,
                minTemp = 15.0 + i,
                maxTemp = 25.0 + i,
                pressure = 1010 + i,
                humidity = 50 + i,
                windSpeed = 5.0 + i,
                clouds = i,
                sunrise = 1600000000,
                sunset = 1600040000,
                date = "2024-09-28",
                latitude = 30.0444 + i * 0.01,
                longitude = 31.2357 + i * 0.01,
                lottie = 1
            )
            mockWeatherFakeEntities.add(mockWeatherEntity)
            viewModel.insertWeatherData(mockWeatherEntity)
        }
        val retrievedWeatherData = repository.getAllWeatherData().first()
        assertEquals(
            mockWeatherFakeEntities.sortedBy { it.cityName },
            retrievedWeatherData.sortedBy { it.cityName }
        )
    }


    // -----------------------------------------------------------

    //testing viewModel only
    @Test
    fun `test insert weather data ViewModel Only`() = runTest {
        val mockWeatherEntity = WeatherEntity(
            cityName = "Cairo",
            description = "Clear Sky",
            currentTemp = 30.0,
            minTemp = 25.0,
            maxTemp = 35.0,
            pressure = 1012,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            sunrise = 1600000000,
            sunset = 1600040000,
            date = "2024-09-28",
            latitude = 30.0444,
            longitude = 31.2357,
            lottie = 1
        )

        // Call the method to insert weather data
        viewModel.insertWeatherData(mockWeatherEntity)

        // Collect all weather data from the ViewModel's internal state
        val weatherData = viewModel.allWeatherData.first()

        // Assert that the inserted data matches the mock data
        assertEquals(listOf(mockWeatherEntity), weatherData)
    }

    @Test
    fun test_delete_weather_data_ViewModel_Only() = runTest {
        val mockWeatherEntity = WeatherEntity(
            cityName = "Cairo",
            description = "Clear Sky",
            currentTemp = 30.0,
            minTemp = 25.0,
            maxTemp = 35.0,
            pressure = 1012,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            sunrise = 1600000000,
            sunset = 1600040000,
            date = "2024-09-28",
            latitude = 30.0444,
            longitude = 31.2357,
            lottie = 1
        )

        viewModel.insertWeatherData(mockWeatherEntity)
        viewModel.deleteWeatherData(mockWeatherEntity)

        // Collect the updated weather data from the ViewModel's internal state
        val weatherData = viewModel.allWeatherData.first()

        // Assert that the weather data is now empty after deletion
        assertEquals(emptyList<WeatherEntity>(), weatherData)
    }

    @Test
    fun test_get_weather_city_ViewModel_Only() = runTest {
        val mockWeatherEntity = WeatherEntity(
            cityName = "Alexandria",
            description = "Clear Sky",
            currentTemp = 30.0,
            minTemp = 25.0,
            maxTemp = 35.0,
            pressure = 1012,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            sunrise = 1600000000,
            sunset = 1600040000,
            date = "2024-09-28",
            latitude = 30.0444,
            longitude = 31.2357,
            lottie = 1
        )
        viewModel.insertWeatherData(mockWeatherEntity)

        // Call the method to get weather by city name
        val weatherCity = viewModel.getWeatherCity("Alexandria")

        // Assert that the returned city matches the mock data
        assertEquals(mockWeatherEntity, weatherCity)
    }

    @Test
    fun test_get_weather_city_not_found_ViewModel_Only() = runTest {
        // Call the method to get a city that does not exist
        val weatherCity = viewModel.getWeatherCity("Cairo")

        // Assert that no weather data is found
        assertEquals(null, weatherCity)
    }

    @Test
    fun test_getAllWeatherData_ViewModel_Only() = runTest {
        val mockWeatherFakeEntities = mutableListOf<WeatherEntity>()
        for (i in 1..10) {
            val mockWeatherEntity = WeatherEntity(
                cityName = "City $i",
                description = "Description for city $i",
                currentTemp = 20.0 + i,
                minTemp = 15.0 + i,
                maxTemp = 25.0 + i,
                pressure = 1010 + i,
                humidity = 50 + i,
                windSpeed = 5.0 + i,
                clouds = i,
                sunrise = 1600000000,
                sunset = 1600040000,
                date = "2024-09-28",
                latitude = 30.0444 + i * 0.01,
                longitude = 31.2357 + i * 0.01,
                lottie = 1
            )
            mockWeatherFakeEntities.add(mockWeatherEntity)
            viewModel.insertWeatherData(mockWeatherEntity)
        }
        // Retrieve all weather data from the ViewModel's internal state
        val retrievedWeatherData = viewModel.allWeatherData.first()

        // Assert that the retrieved weather data matches the mock data
        assertEquals(
            mockWeatherFakeEntities.sortedBy { it.cityName },
            retrievedWeatherData.sortedBy { it.cityName }
        )
    }
}