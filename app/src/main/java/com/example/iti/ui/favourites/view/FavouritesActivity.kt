package com.example.iti.ui.favourites.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iti.databinding.ActivityFavouritesBinding
import com.example.iti.db.local.LocalDataSourceImpl
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.db.sharedPrefrences.SettingsDataSourceImpl
import com.example.iti.network.ApiClient
import com.example.iti.ui.favourites.viewModel.FavouritesAdapter
import com.example.iti.ui.favourites.viewModel.FavouritesViewModel
import com.example.iti.ui.favourites.viewModel.FavouritesViewModelFactory
import com.example.iti.ui.googleMaps.GoogleMapsActivity
import com.example.iti.ui.homeScreen.view.HomeScreenActivity
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.ui.settings.viewModel.SettingsViewModelFactory
import kotlinx.coroutines.launch

class FavouritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouritesBinding
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
    private lateinit var favouritesAdapter: FavouritesAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpRecyclerViews()
        setUpObservers()
        onButtonClicks()
    }

    private fun setUpRecyclerViews() {
        favouritesAdapter = FavouritesAdapter(settingsViewModel, lifecycleScope) { cityName ->
            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.putExtra("CITY_KEY", cityName)
            Log.e("FavouritesActivity", "City Name: $cityName")
            startActivity(intent)
        }

        binding.rvFavs.layoutManager = LinearLayoutManager(this)
        binding.rvFavs.adapter = favouritesAdapter
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            favouritesViewModel.allWeatherData.collect {
                favouritesAdapter.submitList(it)
            }
        }
    }

    private fun onButtonClicks() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnMaps.setOnClickListener {
            startActivity(Intent(this, GoogleMapsActivity::class.java))
        }
    }
}