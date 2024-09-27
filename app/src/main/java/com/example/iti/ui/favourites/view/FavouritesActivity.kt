package com.example.iti.ui.favourites.view

import SwipeToDeleteCallback
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iti.R
import com.example.iti.databinding.ActivityFavouritesBinding
import com.example.iti.db.local.favourites.LocalDataSourceImpl
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.model.WeatherEntity
import com.example.iti.network.ApiClient
import com.example.iti.ui.favourites.viewModel.FavouritesAdapter
import com.example.iti.ui.favourites.viewModel.FavouritesViewModel
import com.example.iti.ui.favourites.viewModel.FavouritesViewModelFactory
import com.example.iti.ui.googleMaps.view.GoogleMapsActivity
import com.example.iti.ui.homeScreen.view.HomeScreenActivity
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.ui.settings.viewModel.SettingsViewModelFactory
import com.example.iti.utils.Helpers.isNetworkAvailable
import kotlinx.coroutines.launch

class FavouritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouritesBinding
    private val favouritesViewModel: FavouritesViewModel by viewModels {
        FavouritesViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
                sharedPrefsDataSource = SharedPrefsDataSourceImpl(
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
                sharedPrefsDataSource = SharedPrefsDataSourceImpl(
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
            startActivity(intent)
        }

        binding.rvFavs.layoutManager = LinearLayoutManager(this)
        binding.rvFavs.adapter = favouritesAdapter

        // Use the reusable swipe-to-delete callback
        val swipeToDeleteCallback = SwipeToDeleteCallback(
            onSwipedAction = { position ->
                val weatherEntity = favouritesAdapter.currentList[position]
                showDeleteConfirmationDialog(weatherEntity, position)
            },
            iconResId = R.drawable.ic_delete // Pass the delete icon resource
        )

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvFavs)
    }

    private fun showDeleteConfirmationDialog(weatherEntity: WeatherEntity, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Location")
            .setMessage("Are you sure you want to delete this location?")
            .setPositiveButton("Yes") { dialog, _ ->
                favouritesViewModel.deleteWeatherData(weatherEntity)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                favouritesAdapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            favouritesViewModel.allWeatherData.collect { weatherList ->
                if (weatherList.isEmpty()) {
                    binding.tvNoItems.visibility = View.VISIBLE
                    binding.imcNoSaved.visibility = View.VISIBLE
                    binding.rvFavs.visibility = View.GONE
                } else {
                    binding.tvNoItems.visibility = View.GONE
                    binding.imcNoSaved.visibility = View.GONE
                    binding.rvFavs.visibility = View.VISIBLE
                    favouritesAdapter.submitList(weatherList)
                }
            }
        }
    }

    private fun onButtonClicks() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnMaps.setOnClickListener {
            if (isNetworkAvailable(this)) {
                startActivity(Intent(this, GoogleMapsActivity::class.java))
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }
    }
}