package com.example.weather.ui.main

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity
import com.example.weather.R

class DetailedForecastViewModel : ViewModel() {

    var name:String? = null
    var temperatureLabel:String? = null
    var temperature:String? = null
    var icon:String? = null
    var shortForecast:String? = null
    var detailedForecast:String? = null
    var wind:String? = null
    var textColor:Int = R.color.material_on_surface_emphasis_high_type

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

    var index:Int = 0
        set(value) {
            field = value

            val nwsForecast = MainActivity.nwsService.forecast.value
            if (nwsForecast != null && nwsForecast.names.count() > index) {
                val isLowFirst = nwsForecast.names[0] == "Tonight"
                val isLow = !isLowFirst xor (index % 2 == 0)

                temperatureLabel = if (isLow) "Low " else "Hi "
                name = nwsForecast.names[index]
                temperature = "${nwsForecast.temperatures[index]}℉"
                icon = nwsForecast.icons[index]
                shortForecast = nwsForecast.shortForecasts[index]
                detailedForecast = nwsForecast.detailedForecasts[index]
                wind = "Wind ${nwsForecast.windDirections[index]} ${nwsForecast.windSpeeds[index]} MPH"
                textColor = if (isLow) R.color.material_on_surface_emphasis_medium else R.color.material_on_surface_emphasis_high_type
            }
        }
}