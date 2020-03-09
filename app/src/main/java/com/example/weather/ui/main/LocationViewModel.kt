package com.example.weather.ui.main

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity

class LocationViewModel : ViewModel() {
    val location: MutableLiveData<String> =
        MutableLiveData()

    init {
        MainActivity.nwsService.location.observeForever { location -> onLocationChanged(location) } }

    private fun onLocationChanged(location: String?) {
        this.location.postValue(location)
    }
}