package com.example.iti.ui.googleMaps.viewModel

import androidx.lifecycle.ViewModel
import com.example.iti.db.repository.Repository

class MapsViewModel(private val repository: Repository) : ViewModel() {

    fun saveLocation(latitude: Float, longitude: Float) {
        repository.saveLocation(latitude, longitude)
    }

    fun getLocation(): Pair<Float, Float>? {
        return repository.getLocation()
    }

}