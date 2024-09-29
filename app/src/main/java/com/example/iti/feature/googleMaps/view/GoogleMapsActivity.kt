package com.example.iti.feature.googleMaps.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.iti.R
import com.example.iti.data.db.local.favourites.LocalDataSourceImpl
import com.example.iti.data.db.remote.RemoteDataSourceImpl
import com.example.iti.data.db.room.AppDatabase
import com.example.iti.data.db.sharedPrefrences.SharedPrefsDataSourceImpl
import com.example.iti.data.network.ApiClient
import com.example.iti.data.repository.RepositoryImpl
import com.example.iti.databinding.ActivityGoogleMapsBinding
import com.example.iti.databinding.BottomSheetLocationBinding
import com.example.iti.feature.googleMaps.viewModel.MapsViewModel
import com.example.iti.feature.googleMaps.viewModel.MapsViewModelFactory
import com.example.iti.feature.homeScreen.view.HomeScreenActivity
import com.example.iti.utils.Constants.HOME_SCREEN_SHARED_PREFS_NAME
import com.example.iti.utils.Constants.LATITUDE_SHARED
import com.example.iti.utils.Constants.LONGITUDE_SHARED
import com.example.iti.utils.Constants.SHARED_PREFS_NAME
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException
import java.util.Locale

class GoogleMapsActivity : AppCompatActivity() {
    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(
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
                    this.getSharedPreferences(HOME_SCREEN_SHARED_PREFS_NAME, MODE_PRIVATE)
                ),
                localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
            )
        )
    }
    private var lastMarker: Marker? = null
    private var map: GoogleMap? = null
    private lateinit var binding: ActivityGoogleMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            googleMap.setOnMapClickListener { latLng ->
                onMapClicked(latLng)
            }
        }
        setUpGeocoderSearch()
    }

    private fun setUpGeocoderSearch() {
        val placeSearch = binding.placeSearch
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        placeSearch.setAdapter(adapter)

        placeSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = placeSearch.text.toString()
                if (query.isNotBlank()) {
                    searchLocation(query, adapter)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        placeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().length > 2) {
                    searchLocation(p0.toString(), adapter)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        // Handle item click to zoom on the map
        placeSearch.setOnItemClickListener { parent, _, position, _ ->
            val selectedAddress = parent.getItemAtPosition(position) as String
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocationName(selectedAddress, 1)
                if (addresses!!.isNotEmpty()) {
                    val latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 7f)
                    map?.animateCamera(cameraUpdate, 1000, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.error, e.message), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun searchLocation(query: String, adapter: ArrayAdapter<String>) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(query, 5)
            if (!addresses.isNullOrEmpty()) {
                val results =
                    addresses.map { it.getAddressLine(0) ?: getString(R.string.no_address_found) }
                adapter.clear()
                adapter.addAll(results)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, getString(R.string.no_results_found), Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error, e.message), Toast.LENGTH_SHORT).show()
        }
    }


    private fun onMapClicked(latLng: LatLng) {
        // Remove the previous marker if it exists
        lastMarker?.remove()
        lastMarker = map?.addMarker(MarkerOptions().position(latLng))
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        map?.animateCamera(cameraUpdate, 1000, null) // Smooth animation over 1 second

        // Use Geocoder to get the address details
        val geocoder = Geocoder(this, Locale.ENGLISH)
        var addressText = getString(R.string.no_address_found)
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                addressText = address.getAddressLine(0) ?: getString(R.string.no_address_found)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        showLocationBottomSheet(latLng, addressText)
    }

    private fun showLocationBottomSheet(latLng: LatLng, addressText: String) {
        val bottomSheetBinding = BottomSheetLocationBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.latitudeValue.text = latLng.latitude.toString()
        bottomSheetBinding.longitudeValue.text = latLng.longitude.toString()
        bottomSheetBinding.addressValue.text = addressText
        bottomSheetBinding.cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.setAsHome.setOnClickListener {
            //using shared prefs here
            mapsViewModel.saveLocation(latLng.latitude.toFloat(), latLng.longitude.toFloat())

            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.putExtra(LATITUDE_SHARED, latLng.latitude)
            intent.putExtra(LONGITUDE_SHARED, latLng.longitude)
            intent.putExtra("HOMESCREEN", false)
            bottomSheetDialog.dismiss()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        bottomSheetBinding.viewButton.setOnClickListener {
            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.putExtra(LATITUDE_SHARED, latLng.latitude)
            intent.putExtra(LONGITUDE_SHARED, latLng.longitude)
            intent.putExtra("viewOnly", true)
            startActivity(intent)
            finish()
        }
        bottomSheetDialog.show()
    }
}