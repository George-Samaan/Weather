package com.example.iti.feature.favourites.view

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
import com.example.iti.data.db.local.favourites.LocalDataSourceImpl
import com.example.iti.data.db.remote.RemoteDataSourceImpl
import com.example.iti.data.db.room.AppDatabase
import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.data.model.WeatherEntity
import com.example.iti.data.network.ApiClient
import com.example.iti.data.repository.RepositoryImpl
import com.example.iti.databinding.ActivityFavouritesBinding
import com.example.iti.feature.favourites.viewModel.FavouritesAdapter
import com.example.iti.feature.favourites.viewModel.FavouritesViewModel
import com.example.iti.feature.favourites.viewModel.FavouritesViewModelFactory
import com.example.iti.feature.googleMaps.view.GoogleMapsActivity
import com.example.iti.feature.homeScreen.view.HomeScreenActivity
import com.example.iti.feature.settings.viewModel.SettingsViewModel
import com.example.iti.feature.settings.viewModel.SettingsViewModelFactory
import com.example.iti.utils.Constants.FAVOURITE_SHARED_CITY
import com.example.iti.utils.Constants.SHARED_PREFS_NAME
import com.example.iti.utils.Helpers.isNetworkAvailable
import kotlinx.coroutines.launch

class FavouritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouritesBinding
    private val favouritesViewModel: FavouritesViewModel by viewModels {
        FavouritesViewModelFactory(
            RepositoryImpl(
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
        )
    }
    private lateinit var favouritesAdapter: FavouritesAdapter

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            RepositoryImpl(
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
            intent.putExtra(FAVOURITE_SHARED_CITY, cityName)
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
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_location))
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_location))
            .setPositiveButton(R.string.yes) { dialog, _ ->
                favouritesViewModel.deleteWeatherData(weatherEntity)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                favouritesAdapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val buttonOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            buttonOk.setTextColor(resources.getColor(R.color.delete_color, null))
            buttonCancel.setTextColor(resources.getColor(R.color.buttons_, null))
        }
        dialog.show()
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
                Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}