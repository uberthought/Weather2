package com.example.weather.ui.main

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity
import com.example.weather.NWSService
import com.example.weather.R

class ForecastViewModel : ViewModel() {

    class Forecast {
        var name:String? = null
        var icon:String? = null
        var shortForecast:String? = null
        var detailedForecast:String? = null
        var temperatureLabel:String? = null
        var temperature:String? = null
        var wind:String? = null
        var backgroundColor:Int = R.color.dayBackground

        val nameVisibility: Int
            get() = if (name == null) View.GONE else View.VISIBLE
        val iconVisibility: Int
            get() = if (icon == null) View.GONE else View.VISIBLE
        val shortForecastVisibility: Int
            get() = if (shortForecast == null) View.GONE else View.VISIBLE
        val detailedForecastVisibility: Int
            get() = if (detailedForecast == null) View.GONE else View.VISIBLE
        val temperatureVisibility: Int
            get() = if (temperature == null) View.GONE else View.VISIBLE
        val windVisibility: Int
            get() = if (wind == null) View.GONE else View.VISIBLE
    }

    val forecasts: MutableLiveData<MutableList<Forecast>> = MutableLiveData()

    init {
        MainActivity.nwsService.forecast.observeForever { forecast -> onForecastChanged(forecast) }
    }

    private fun onForecastChanged(nwsForecast: NWSService.Forecast) {
        val isLowFirst = nwsForecast.names[0] == "Tonight"

        val forecasts = mutableListOf<Forecast>()
        for (i in 0 until nwsForecast.names.count()) {
            val isLow = !isLowFirst xor (i % 2 == 0)
            val forecast = Forecast()
            forecast.name = nwsForecast.names[i]
            forecast.icon = nwsForecast.icons[i]
            forecast.shortForecast = nwsForecast.shortForecasts[i]
            forecast.detailedForecast = nwsForecast.detailedForecasts[i]
            forecast.temperatureLabel = if (isLow) "Low " else "Hi "
            forecast.temperature = "${nwsForecast.temperatures[i]}℉"
            forecast.wind = "Wind ${nwsForecast.windDirections[i]} ${nwsForecast.windSpeeds[i]} MPH"
            forecast.backgroundColor = if (isLow) R.color.nightBackground else R.color.dayBackground
            forecasts.add(forecast)
        }

        this.forecasts.postValue(forecasts)
    }

    fun count():Int {
        forecasts.value ?: return 0
        return forecasts.value!!.count()
    }
    operator fun get(i:Int):Forecast = forecasts.value!![i]
}
