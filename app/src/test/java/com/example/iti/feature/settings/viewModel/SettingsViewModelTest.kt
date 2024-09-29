package com.example.iti.feature.settings.viewModel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iti.data.repository.Repository
import com.example.iti.utils.Constants.CELSIUS_SHARED
import com.example.iti.utils.Constants.FAHRENHEIT_SHARED
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// TEST VIEWMODEL
@RunWith(AndroidJUnit4::class)
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var mockRepository: Repository

    @Before
    fun setup() {
        mockRepository = FakeRepository()
        viewModel = SettingsViewModel(mockRepository)
    }

    @Test
    fun test_get_and_set_temperature_unit() {
        assert(viewModel.getTemperatureUnit() == CELSIUS_SHARED)
        viewModel.setTemperatureUnit(FAHRENHEIT_SHARED)
        assert(viewModel.getTemperatureUnit() == FAHRENHEIT_SHARED)
    }

    @Test
    fun test_get_and_set_wind_speed_unit() {
        assert(viewModel.getWindSpeedUnit() == "km/h")
        viewModel.setWindSpeedUnit("mph")
        assert(viewModel.getWindSpeedUnit() == "mph")
    }

    @Test
    fun test_get_and_set_notification_preference() {
        assert(viewModel.getNotificationPreference() == true)
        viewModel.setNotificationPreference(false)
        assert(viewModel.getNotificationPreference() == false)
    }
}