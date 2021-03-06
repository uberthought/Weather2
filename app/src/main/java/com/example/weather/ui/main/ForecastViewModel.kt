package com.example.weather.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.R
import com.example.weather.Services.NWSService

class ForecastViewModel : ViewModel() {
    val forecasts: MutableLiveData<List<DataViewModel>> = MutableLiveData()

    init { NWSService.instance.forecasts.observeForever { forecasts -> onForecastsChanged(forecasts) } }

    private fun onForecastsChanged(forecasts: List<NWSService.Forecast>) {
        this.forecasts.postValue(forecasts.map {
            DataViewModel(
                title = it.name,
                icon = it.icon,
                shortDescription = it.shortForecast,
                detailedDescription = it.detailedForecast,
                temperatureLabel = if (it.isDaytime) "Hi " else "Low ",
                temperature = "%.0f℉".format(it.temperature),
                wind = "Wind ${it.windDirection} ${it.windSpeed}".replace("mph", "MPH"),
                temperatureTrend = if (it.temperatureTrend == null) null else "(${it.temperatureTrend})",
                backgroundColor = if (it.isDaytime) R.color.primaryColor else R.color.secondaryColor
            )
        })
    }
}