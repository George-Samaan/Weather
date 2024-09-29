package com.example.iti.repo

import com.example.iti.data.db.local.favourites.LocalDataSource
import com.example.iti.data.db.remote.RemoteDataSource
import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSource
import com.example.iti.data.model.WeatherEntity
import com.example.iti.data.repository.Repository
import com.example.iti.data.repository.RepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class RepositoryTest {
    private lateinit var localDataSource: LocalDataSource
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var sharedPrefsDataSource: SharedPrefsDataSource
    private lateinit var repository: Repository

    @Before
    fun setup() {
        localDataSource = FakeLocalDataSource()
        remoteDataSource = FakeRemoteDataSource()
        sharedPrefsDataSource = FakeSharedPrefsDataSource()
        repository = RepositoryImpl(remoteDataSource, sharedPrefsDataSource, localDataSource)
    }

    @Test
    fun `test insert weather data`() = runTest {
        val mockWeatherEntity = mockWeatherEntity
        repository.insertWeather(mockWeatherEntity)
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(listOf(mockWeatherEntity), weatherData)
    }

    @Test
    fun test_delete_weather_data() = runTest {
        val mockWeatherEntity = mockWeatherEntity
        repository.insertWeather(mockWeatherEntity)
        repository.deleteWeather(mockWeatherEntity)
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(emptyList<WeatherEntity>(), weatherData)
    }

    @Test
    fun test_inserting_more_than_one_weather_data() = runTest {
        val mockWeatherEntity = mockWeatherEntity
        val mockWeatherEntity2 = mockWeatherEntity2
        repository.insertWeather(mockWeatherEntity)
        repository.insertWeather(mockWeatherEntity2)
        repository.insertWeather(mockWeatherEntity2)
        repository.deleteWeather(mockWeatherEntity)
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(listOf(mockWeatherEntity2, mockWeatherEntity2), weatherData)
    }


    @Test
    fun test_get_weather_city_not_found() = runTest {
        //     repository.insertWeather(mockWeatherEntity)
        val weatherCity = repository.getWeatherCity("Cairo")
        assertEquals(null, weatherCity)
    }

    @Test
    fun test_get_weather_city() = runTest {
        val mockWeatherEntity = mockWeatherEntity
        repository.insertWeather(mockWeatherEntity)
        val weatherCity = repository.getWeatherCity("Cairo")
        assertEquals(mockWeatherEntity, weatherCity)
    }
}


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

val mockWeatherEntity2 = WeatherEntity(
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