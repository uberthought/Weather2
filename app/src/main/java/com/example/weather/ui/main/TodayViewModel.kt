package com.example.weather.ui.main

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity
import com.example.weather.NWSService
import com.example.weather.R
import java.text.SimpleDateFormat

class TodayViewModel() : ViewModel() {

    class Conditions {
        var dewPoint:String? = null
        var relativeHumidity:String? = null
        var temperature:String? = null
        var wind:String? = null
        var windGust:String? = null
        var icon:String? = null
        var textDescription:String? = null
        var timestamp:String? = null

        val dewPointVisibility: Int
            get() = if (dewPoint == null) View.GONE else View.VISIBLE
        val relativeHumidityVisibility: Int
            get() = if (relativeHumidity == null) View.GONE else View.VISIBLE
        val temperatureVisibility: Int
            get() = if (temperature == null) View.GONE else View.VISIBLE
        val windVisibility: Int
            get() = if (wind == null) View.GONE else View.VISIBLE
        val windGustVisibility: Int
            get() = if (windGust == null) View.GONE else View.VISIBLE
        val iconVisibility: Int
            get() = if (icon == null) View.GONE else View.VISIBLE
        val textDescriptionVisibility: Int
            get() = if (textDescription == null) View.GONE else View.VISIBLE
        val timestampVisibility: Int
            get() = if (timestamp == null) View.GONE else View.VISIBLE
    }

    class Forecast {
        var label:String? = null
        var temperature:String? = null
        var icon:String? = null
        var detailedForecast:String? = null
        var backgroundColor:Int = R.color.dayBackground

        val labelVisibility: Int
            get() = if (label == null) View.GONE else View.VISIBLE
        val temperatureVisibility: Int
            get() = if (temperature == null) View.GONE else View.VISIBLE
        val iconVisibility: Int
            get() = if (icon == null) View.GONE else View.VISIBLE
        val detailedForecastVisibility: Int
            get() = if (detailedForecast == null) View.GONE else View.VISIBLE
    }

    val locationVisibility: Int
        get() = if (location.value == null) View.GONE else View.VISIBLE

    val location:MutableLiveData<String> = MutableLiveData()
    val conditions:MutableLiveData<Conditions> = MutableLiveData()
    val forecast:MutableLiveData<Forecast> = MutableLiveData()

    init {
        MainActivity.nwsService.location.observeForever { location -> onLocationChanged(location) }
        MainActivity.nwsService.conditions.observeForever { conditions -> onConditionsChanged(conditions) }
        MainActivity.nwsService.forecast.observeForever { forecast -> onForecastChanged(forecast) }
    }

    private fun onLocationChanged(location:String) {
        if (location.isNotEmpty())
            this.location.postValue(location)
    }

    private fun onConditionsChanged(nwsConditions: NWSService.Conditions) {
        val conditions = Conditions()

        conditions.dewPoint = getDewPoint(nwsConditions)
        conditions.relativeHumidity = getHumidity(nwsConditions)
        conditions.temperature = getTemperature(nwsConditions)
        conditions.wind = getWind(nwsConditions)
        conditions.windGust = getGusts(nwsConditions)
        conditions.icon = nwsConditions.icon
        conditions.textDescription = nwsConditions.textDescription
        conditions.timestamp = getTimestamp(nwsConditions)

        this.conditions.postValue(conditions)
    }

    private fun getTimestamp(nwsConditions: NWSService.Conditions): String? {
        nwsConditions.timestamp ?: return null
        return SimpleDateFormat("h:mm a MMM d, yyyy").format(nwsConditions.timestamp!!)
    }

    private fun onForecastChanged(nwsForecast: NWSService.Forecast) {
        val forecast = Forecast()

        if (nwsForecast.names.count() > 0) {
            val isLow = nwsForecast.names[0] == "Tonight"
            forecast.temperature = getForecastTemperature(nwsForecast)
            forecast.label = nwsForecast.names[0]
            forecast.icon = nwsForecast.icons[0]
            forecast.detailedForecast = nwsForecast.detailedForecasts[0]
            forecast.backgroundColor = if (isLow) R.color.nightBackground else R.color.dayBackground
        }
        this.forecast.postValue(forecast)
    }

    private fun toFahrenheit(celsius:Double):Double = celsius * 9.0 / 5.0 + 32.0

    private fun toMPH(metersPerSecond:Double):Double = metersPerSecond * 2.237

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

    private fun getForecastTemperature(nwsForecast: NWSService.Forecast): String? {
        val temperature = nwsForecast.temperatures[0]
        val name = nwsForecast.names[0]
        if (name == "Tonight") {
            return "Low ${temperature}℉"
        }
        return "Hi ${temperature}℉"
    }

    private fun getHumidity(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.relativeHumidity ?: return null
        return "Humidity %.0f%%".format(nwsConditions.relativeHumidity!!)
    }

    private fun getWind(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.windDirection ?: return null
        nwsConditions.windSpeed ?: return null

        val angle = nwsConditions.windDirection!!
        var windDirection = "";
        if (angle > 337.5 || angle <= 22.5)
            windDirection = "N";
        else if (angle > 22.5 && angle <= 67.5)
            windDirection = "NE";
        else if (angle > 67.5 && angle <= 112.5)
            windDirection = "E";
        else if (angle > 112.5 && angle <= 157.5)
            windDirection = "SE";
        else if (angle > 157.5 && angle <= 202.5)
            windDirection = "S";
        else if (angle > 202.5 && angle <= 247.5)
            windDirection = "SW";
        else if (angle > 247.5 && angle <= 292.5)
            windDirection = "W";
        else if (angle > 292.5 && angle <= 337.5)
            windDirection = "NW";

        val windSpeed = "%.1f".format(toMPH(nwsConditions.windSpeed!!))
        return "Wind ${windDirection} ${windSpeed}MPH"
    }

    private fun getGusts(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.windGust ?: return null
        val windGust = "%.1f".format(toMPH(nwsConditions.windGust!!))
        return "Gusts ${windGust}MPH"
    }
}
