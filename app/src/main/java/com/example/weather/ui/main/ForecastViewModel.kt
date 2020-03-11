package com.example.weather.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.R
import com.example.weather.Services.NWSService

class ForecastViewModel : ViewModel() {
    val forecasts: MutableLiveData<MutableList<DataViewModel>> =
        MutableLiveData()

    init {
        NWSService.instance.forecast.observeForever { forecast -> onForecastChanged(forecast) }
    }

    private fun onForecastChanged(nwsForecast: NWSService.Forecast) {
        val isLowFirst = nwsForecast.names[0] == "Tonight" || nwsForecast.names[0] == "Overnight"
        val forecasts = mutableListOf<DataViewModel>()
        for (i in 0 until nwsForecast.names.count()) {
            val isLow = isLowFirst xor (i % 2 == 1)
            val forecast = DataViewModel()
            with(forecast) {
                title = nwsForecast.names[i]
                icon = nwsForecast.icons[i]
                shortDescription = nwsForecast.shortForecasts[i]
                detailedDescription = nwsForecast.detailedForecasts[i]
                temperatureLabel = if (isLow) "Low " else "Hi "
                temperature = "${nwsForecast.temperatures[i]}℉"
                wind = "Wind ${nwsForecast.windDirections[i]} ${nwsForecast.windSpeeds[i]}".replace("mph", "MPH")
                backgroundColor = if (isLow) R.color.secondaryColor else R.color.primaryColor
            }
            forecasts.add(forecast)
        }
        this.forecasts.postValue(forecasts)
    }
}