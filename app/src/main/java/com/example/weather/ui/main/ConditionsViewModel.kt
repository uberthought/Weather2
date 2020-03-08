package com.example.weather.ui.main

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity
import com.example.weather.NWSService
import com.example.weather.R
import java.text.SimpleDateFormat

class ConditionsViewModel : ViewModel() {

    class Conditions {
        var title:String? = null
        var shortDescription:String? = null
        var detailedDescription:String? = null
        var temperatureLabel:String? = null
        var temperature:String? = null
        var wind:String? = null

        var dewPoint:String? = null
        var relativeHumidity:String? = null
        var windGust:String? = null
        var visibility:String? = null
        var pressure:String? = null
        var windChill:String? = null
        var heatIndex:String? = null

        var icon:String? = null
        var textColor:Int = R.color.material_on_surface_emphasis_high_type

        val titleVisibility: Int
            get() = if (title == null) View.GONE else View.VISIBLE
        val iconVisibility: Int
            get() = if (icon == null) View.GONE else View.VISIBLE
        val shortDescriptionVisibility: Int
            get() = if (shortDescription == null) View.GONE else View.VISIBLE
        val detailedDescriptionVisibility: Int
            get() = if (detailedDescription == null) View.GONE else View.VISIBLE
        val temperatureVisibility: Int
            get() = if (temperature == null) View.GONE else View.VISIBLE
        val windVisibility: Int
            get() = if (wind == null) View.GONE else View.VISIBLE

        val dewPointVisibility: Int
            get() = if (dewPoint == null) View.GONE else View.VISIBLE
        val relativeHumidityVisibility: Int
            get() = if (relativeHumidity == null) View.GONE else View.VISIBLE
        val windGustVisibility: Int
            get() = if (windGust == null) View.GONE else View.VISIBLE
        val visibilityVisibility: Int
            get() = if (visibility == null) View.GONE else View.VISIBLE
        val pressureVisibility: Int
            get() = if (pressure == null) View.GONE else View.VISIBLE
        val windChillVisibility: Int
            get() = if (windChill == null) View.GONE else View.VISIBLE
        val heatIndexVisibility: Int
            get() = if (heatIndex == null) View.GONE else View.VISIBLE
    }

    val location: MutableLiveData<String> = MutableLiveData()
    val timestamp: MutableLiveData<String> = MutableLiveData()

    val conditions: MutableLiveData<Conditions> = MutableLiveData()
    val forecasts: MutableLiveData<MutableList<Conditions>> = MutableLiveData()

    init {
        with(MainActivity.nwsService) {
            location.observeForever { location -> onLocationChanged(location) }
            forecast.observeForever { forecast -> onForecastChanged(forecast) }
            conditions.observeForever { conditions -> onConditionsChanged(conditions) }
        }
    }

    private fun onLocationChanged(location: String) {
        this.location.postValue((location))
    }

    private fun onConditionsChanged(nwsConditions: NWSService.Conditions) {
        val conditions = Conditions()
        with(conditions) {
            title = "Current Conditions"
            dewPoint = getDewPoint(nwsConditions)
            relativeHumidity = getHumidity(nwsConditions)
            temperature = getTemperature(nwsConditions)
            wind = getWind(nwsConditions)
            windGust = getGusts(nwsConditions)
            icon = nwsConditions.icon
            shortDescription = nwsConditions.textDescription
            visibility = getVisibility(nwsConditions)
            pressure = getPressure(nwsConditions)
            windChill = getWindChill(nwsConditions)
            heatIndex = getHeatIndex(nwsConditions)
        }
        this.conditions.postValue(conditions)
        this.timestamp.postValue(getTimestamp(nwsConditions))
    }

    private fun onForecastChanged(nwsForecast: NWSService.Forecast) {
        val isLowFirst = nwsForecast.names[0] == "Tonight"
        val forecasts = mutableListOf<Conditions>()
        for (i in 0 until nwsForecast.names.count()) {
            val isLow = !isLowFirst xor (i % 2 == 0)
            val forecast = Conditions()
            with(forecast) {
                title = nwsForecast.names[i]
                icon = nwsForecast.icons[i]
                shortDescription = nwsForecast.shortForecasts[i]
                detailedDescription = nwsForecast.detailedForecasts[i]
                temperatureLabel = if (isLow) "Low " else "Hi "
                temperature = "${nwsForecast.temperatures[i]}℉"
                wind = "Wind ${nwsForecast.windDirections[i]} ${nwsForecast.windSpeeds[i]}".replace("mph", "MPH")
                textColor = if (isLow) R.color.material_on_surface_emphasis_medium else R.color.material_on_surface_emphasis_high_type
            }
            forecasts.add(forecast)
        }
        this.forecasts.postValue(forecasts)
    }

    fun count():Int {
        var count = 0
        if (conditions.value != null) count++
        if (forecasts.value != null) count += forecasts.value!!.count()
        return count
    }

    operator fun get(i:Int):Conditions {
        return when {
            conditions.value != null && i == 0 -> conditions.value!!
            conditions.value != null && i != 0 -> forecasts.value!![i-1]
            else -> forecasts.value!![i]
        }
    }

    private fun toFahrenheit(celsius:Double) = celsius * 9.0 / 5.0 + 32.0

    private fun toMPH(metersPerSecond:Double) = metersPerSecond * 2.237

    private fun toMiles(meters:Double) = meters / 1609.0

    private fun toHG(pascals:Double) = pascals / 3386.0

    private fun getTimestamp(nwsConditions: NWSService.Conditions): String? {
        nwsConditions.timestamp ?: return null
        val timeString = SimpleDateFormat.getDateTimeInstance().format(nwsConditions.timestamp!!)
        return "Update $timeString"
    }
    private fun getDewPoint(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.dewPoint ?: return null
        val fahrenheit = toFahrenheit(nwsConditions.dewPoint!!)
        return "Dew Point %.0f℉".format(fahrenheit)
    }

    private fun getTemperature(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.temperature ?: return null
        val fahrenheit = toFahrenheit(nwsConditions.temperature!!)
        return "%.0f℉".format(fahrenheit)
    }

    private fun getHumidity(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.relativeHumidity ?: return null
        return "Humidity %.0f%%".format(nwsConditions.relativeHumidity!!)
    }

    private fun getWind(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.windDirection ?: return null
        nwsConditions.windSpeed ?: return null

        val angle = nwsConditions.windDirection!!
        val windDirection = when {
            angle > 337.5 || angle <= 22.5 -> "N"
            angle > 22.5 && angle <= 67.5 -> "NE"
            angle > 67.5 && angle <= 112.5 -> "E"
            angle > 112.5 && angle <= 157.5 -> "SE"
            angle > 157.5 && angle <= 202.5 -> "S"
            angle > 202.5 && angle <= 247.5 -> "SW"
            angle > 247.5 && angle <= 292.5 -> "W"
            angle > 292.5 && angle <= 337.5 -> "NW"
            else -> ""
        }

        val windSpeed = "%.1f".format(toMPH(nwsConditions.windSpeed!!))
        return "Wind $windDirection $windSpeed MPH"
    }

    private fun getGusts(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.windGust ?: return null
        val windGust = "%.1f".format(toMPH(nwsConditions.windGust!!))
        return "Gusts $windGust MPH"
    }
    private fun getVisibility(nwsConditions: NWSService.Conditions): String? {
        nwsConditions.visibility ?: return null
        val visibility = "%.1f".format(toMiles(nwsConditions.visibility!!))
        return "Visibility $visibility Miles"
    }

    private fun getPressure(nwsConditions: NWSService.Conditions): String? {
        nwsConditions.barometricPressure ?: return null
        val pressure = "%.2f".format(toHG(nwsConditions.barometricPressure!!))
        return "Pressure $pressure HG"
    }

    private fun getWindChill(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.windChill ?: return null
        val farenheit = toFahrenheit(nwsConditions.windChill!!)
        return "Wind Chill %.0f℉".format(farenheit)
    }

    private fun getHeatIndex(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.heatIndex ?: return null
        val fahrenheit = toFahrenheit(nwsConditions.heatIndex!!)
        return "Heat Index %.0f℉".format(fahrenheit)
    }
}
