package com.example.iti.ui.googleMaps

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.iti.R
import com.example.iti.databinding.ActivityGoogleMapsBinding
import com.example.iti.databinding.BottomSheetLocationBinding
import com.example.iti.ui.homeScreen.view.HomeScreenActivity
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
    /*private lateinit var placesClient: PlacesClient
    private lateinit var token: AutocompleteSessionToken
    private lateinit var predictions: List<AutocompletePrediction>*/

    private var lastMarker: Marker? = null // Add a variable to store the last marker
    private var map: GoogleMap? = null
    private lateinit var binding: ActivityGoogleMapsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Google Places API
        /*      Places.initialize(applicationContext, "AIzaSyBjX2M6vs7EUkgW32X1av0kp1hYvIlOyBM")
              placesClient = Places.createClient(this)
              token = AutocompleteSessionToken.newInstance()*/

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            googleMap.setOnMapClickListener { latLng ->
                onMapClicked(latLng)
            }
        }

        // Set up Place Autocomplete functionality
//        setupPlaceAutocomplete()
    }


    /*            private fun setupPlaceAutocomplete() {
                val placeSearch = findViewById<AutoCompleteTextView>(R.id.place_search)

                // Set up a simple ArrayAdapter for displaying predictions
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
                placeSearch.setAdapter(adapter)

                // Handle the Enter/Done key
                placeSearch.setOnEditorActionListener { textView, actionId, keyEvent ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                        placeSearch.dismissDropDown()
                        return@setOnEditorActionListener true
                    }
                    false
                }

                // Listen for text input changes
                placeSearch.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s != null && s.length > 2) {
                            // Fetch predictions if the query is longer than 2 characters
                            getPlacePredictions(s.toString(), adapter)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                // Handle item click when a user selects a suggestion
                placeSearch.setOnItemClickListener { parent, view, position, id ->
                    val prediction = predictions[position]
                    val placeId = prediction.placeId

                    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                    val placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

                    placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener { placeResponse ->
                            val place = placeResponse.place
                            val latLng = place.latLng
                            if (latLng != null) {
                                // Move the camera to the selected place on the map
                                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }

            private fun getPlacePredictions(query: String, adapter: ArrayAdapter<String>) {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setSessionToken(token)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        adapter.clear()
                        predictions =
                            response.autocompletePredictions // Save predictions to the class variable
                        for (prediction in predictions) {
                            adapter.add(prediction.getPrimaryText(null).toString())
                        }
                        adapter.notifyDataSetChanged()

                        if (predictions.isEmpty()) {
                            Log.d("GoogleMapsActivity", "No predictions found.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        Log.e("GoogleMapsActivity", "Failed to fetch predictions: ${exception.message}")
                    }
            }

    */


    private fun onMapClicked(latLng: LatLng) {
        // Remove the previous marker if it exists
        lastMarker?.remove()

        // Add a new marker at the clicked location and store the reference
        lastMarker = map?.addMarker(MarkerOptions().position(latLng))

        // Animate the camera to the clicked location with a zoom level of 12
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        map?.animateCamera(cameraUpdate, 1000, null) // Smooth animation over 1 second

        // Use Geocoder to get the address details
        val geocoder = Geocoder(this, Locale.ENGLISH)
        var addressText: String = "Address not found"

        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                // Construct the address text (you can customize this as needed)
                addressText = address.getAddressLine(0) ?: "Address not found"
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

        bottomSheetBinding.saveButton.setOnClickListener {
            Log.e("GoogleMapsActivity", "Location saved:${latLng.latitude}, ${latLng.longitude}")
            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.putExtra("latitude", latLng.latitude)
            intent.putExtra("longitude", latLng.longitude)
            startActivity(intent)
            finish()
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.cancelButton.setOnClickListener {
            // Handle cancel button click
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
}




