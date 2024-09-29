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
        val fakeWeatherEntity = fakeWeatherEntity
        repository.insertWeather(fakeWeatherEntity)
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(listOf(fakeWeatherEntity), weatherData)
    }

    @Test
    fun test_retrieve_nothing_weather_data() = runTest {
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(emptyList<WeatherEntity>(), weatherData)
    }


    @Test
    fun test_delete_weather_data() = runTest {
        val fakeWeatherEntity = fakeWeatherEntity
        repository.insertWeather(fakeWeatherEntity)
        repository.deleteWeather(fakeWeatherEntity)
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(emptyList<WeatherEntity>(), weatherData)
    }

    @Test
    fun test_inserting_more_than_one_weather_data() = runTest {
        val fakeWeatherEntity = fakeWeatherEntity
        val fakeWeatherEntity2 = fakeWeatherEntity2
        repository.insertWeather(fakeWeatherEntity)
        repository.insertWeather(fakeWeatherEntity2)
        repository.insertWeather(fakeWeatherEntity2)
        repository.deleteWeather(fakeWeatherEntity)
        val weatherData = repository.getAllWeatherData().first()
        assertEquals(listOf(fakeWeatherEntity2, fakeWeatherEntity2), weatherData)
    }


    @Test
    fun test_get_weather_city_not_found() = runTest {
        //     repository.insertWeather(mockWeatherEntity)
        val weatherCity = repository.getWeatherCity("Cairo")
        assertEquals(null, weatherCity)
    }

    @Test
    fun test_get_weather_city() = runTest {
        val fakeWeatherEntity = fakeWeatherEntity
        repository.insertWeather(fakeWeatherEntity)
        val weatherCity = repository.getWeatherCity("Cairo")
        assertEquals(fakeWeatherEntity, weatherCity)
    }


    //-------------------------------------------------------------------------
    // test remote fetch current weather
    @Test
    fun test_fetch_current_weather() = runTest {
        val weather = repository.fetchCurrentWeather(37.39, -122.08).first()
        assertEquals("San Francisco", weather.name)
        assertEquals("Clear", weather.weather[0].main)
        assertEquals(283, weather.main.temp.toInt())
    }

    val fakeWeatherEntity = WeatherEntity(
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

    val fakeWeatherEntity2 = WeatherEntity(
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
}