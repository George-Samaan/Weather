package com.example.iti.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.iti.databinding.ActivitySplashBinding
import com.example.iti.ui.googleMaps.GoogleMapsActivity
import com.example.iti.ui.homeScreen.view.HomeScreenActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var permissionDeniedCounts = 0
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    if (isLocationEnabled()) {
                        getFreshLocation() // Fetch fresh location when enabled
                    } else {
                        fetchLocationFromSharedPreferences() // Fetch from SharedPreferences if location is disabled
                    }
                } else {
                    permissionDeniedCounts++ // Increment denial count
                    if (permissionDeniedCounts < 2) {
                        showEnableLocationDialog() // Show dialog to ask user
                    }
                }
            }

        Handler(mainLooper).postDelayed({ checkLocationPermission() }, 3000)
    }

    // Check if location permission is granted
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (isLocationEnabled()) {
                    getFreshLocation() // Get current lat & long
                } else {
                    fetchLocationFromSharedPreferences() // Get lat & long from SharedPreferences
                }
            }
            // Show rationale for the permission
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    this,
                    "Location permission is required to access home screen features.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            // Request permission for the first time
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Check if location services (GPS) are enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Fetch location from SharedPreferences
    private fun fetchLocationFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("homeScreen", Context.MODE_PRIVATE)
        val savedLatitude = sharedPreferences.getFloat("latitude", Float.MIN_VALUE)
        val savedLongitude = sharedPreferences.getFloat("longitude", Float.MIN_VALUE)

        if (savedLatitude != Float.MIN_VALUE && savedLongitude != Float.MIN_VALUE) {
            // Latitude and Longitude are already stored, navigate to HomeScreenActivity
            val intent = Intent(this, HomeScreenActivity::class.java).apply {
                putExtra("latitude", savedLatitude.toDouble())
                putExtra("longitude", savedLongitude.toDouble())
            }
            startActivity(intent)
            finish()
        } else {
            // Show message if location is not stored
            Toast.makeText(
                this,
                "No location saved. Please enable location services.",
                Toast.LENGTH_LONG
            ).show()
            showEnableLocationDialog()
        }
    }

    // Show AlertDialog to ask if the user wants to enable location services
    private fun showEnableLocationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location Services")
            .setMessage("Location services are required to access the home screen. Do you want to enable them?")
            .setPositiveButton("Yes") { _, _ -> promptEnableLocationServices() }
            .setNegativeButton("Choose From Maps") { _, _ -> navigateToGoogleMaps() }
            .setCancelable(false)
            .show()
    }

    // Prompt user to enable location services by navigating to settings
    private fun promptEnableLocationServices() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    @Synchronized
    @SuppressLint("MissingPermission")
    private fun getFreshLocation() {
        val locationRequest: LocationRequest = LocationRequest.Builder(500).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()

        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("Location", "Longitude: $longitude, Latitude: $latitude")
                    navigateToHomeScreen()
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeScreenActivity::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToGoogleMaps() {
        startActivity(Intent(this, GoogleMapsActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && isLocationEnabled()
        ) {
            getFreshLocation()
        }
    }
}