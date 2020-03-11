package com.example.weather.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.Services.NWSService

class LocationViewModel : ViewModel() {
    val location: MutableLiveData<String> =
        MutableLiveData()

    init {
        NWSService.instance.location.observeForever { location -> onLocationChanged(location) } }

    private fun onLocationChanged(location: String?) {
        this.location.postValue(location)
    }
}