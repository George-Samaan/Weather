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
import com.example.iti.R
import com.example.iti.databinding.ActivitySplashBinding
import com.example.iti.pushNotifications.Permission
import com.example.iti.ui.googleMaps.view.GoogleMapsActivity
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
        Permission.NotificationPermission.requestNotificationPermission(this)

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
                    getString(R.string.location_permission_is_required_to_access_home_screen_features),
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
                getString(R.string.no_location_saved_please_enable_location_services),
                Toast.LENGTH_LONG
            ).show()
            showEnableLocationDialog()
        }
    }

    // Show AlertDialog to ask if the user wants to enable location services
    private fun showEnableLocationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.enable_location_services))
            .setMessage(getString(R.string.location_services_are_required_to_access_the_home_screen_do_you_want_to_enable_them))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> promptEnableLocationServices() }
            .setNegativeButton(getString(R.string.choose_from_maps)) { _, _ -> navigateToGoogleMaps() }
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val buttonOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            buttonOk.setTextColor(resources.getColor(R.color.buttons_, null))
            buttonCancel.setTextColor(resources.getColor(R.color.buttons_, null))
        }
        dialog.show()
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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