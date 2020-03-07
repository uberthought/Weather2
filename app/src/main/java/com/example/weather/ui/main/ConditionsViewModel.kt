package com.example.weather.ui.main

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity
import com.example.weather.NWSService
import java.text.SimpleDateFormat

class ConditionsViewModel() : ViewModel() {

    var dewpoint:String? = null
    var relativeHumidity:String? = null
    var temperature:String? = null
    var wind:String? = null
    var windGust:String? = null
    var icon:String? = null
    var textDescription:String? = null
    var visibility:String? = null
    var pressure:String? = null
    var windChill:String? = null
    var heatIndex:String? = null
    var timestamp:String? = null
    var location:String? = null

    val dewpointVisibility: Int
        get() = if (dewpoint == null) View.GONE else View.VISIBLE
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
    val visibilityVisibility: Int
        get() = if (visibility == null) View.GONE else View.VISIBLE
    val pressureVisibility: Int
        get() = if (pressure == null) View.GONE else View.VISIBLE
    val windChillVisibility: Int
        get() = if (windChill == null) View.GONE else View.VISIBLE
    val heatIndexVisibility: Int
        get() = if (heatIndex == null) View.GONE else View.VISIBLE
    val timestampVisibility: Int
        get() = if (timestamp == null) View.GONE else View.VISIBLE
    val locationVisibility: Int
        get() = if (location == null) View.GONE else View.VISIBLE

    init {
        MainActivity.nwsService.location.observeForever { location -> onLocationChanged(location) }
        MainActivity.nwsService.conditions.observeForever { conditions -> onConditionsChanged(conditions) }
    }

    fun onLocationChanged(location:String) {
        this.location = location
    }

    fun onConditionsChanged(nwsConditions: NWSService.Conditions) {
        dewpoint = getDewPoint(nwsConditions)
        relativeHumidity = getHumidity(nwsConditions)
        temperature = getTemperature(nwsConditions)
        wind = getWind(nwsConditions)
        windGust = getGusts(nwsConditions)
        icon = nwsConditions.icon
        textDescription = nwsConditions.textDescription
        visibility = getVisibility(nwsConditions)
        pressure = getPressure(nwsConditions)
        windChill = getWindChill(nwsConditions)
        heatIndex = getHeatIndex(nwsConditions)
        timestamp = getTimestamp(nwsConditions)
    }

    private fun toFahrenheit(celcius:Double) = celcius * 9.0 / 5.0 + 32.0

    private fun toMPH(metersPerSecond:Double) = metersPerSecond * 2.237

    private fun toMiles(meters:Double) = meters / 1609.0

    private fun toHG(pascals:Double) = pascals / 3386.0

    private fun getTimestamp(nwsConditions: NWSService.Conditions): String? {
        nwsConditions.timestamp ?: return null
        return SimpleDateFormat("h:mm a MMM d, yyyy").format(nwsConditions.timestamp!!)
    }

    private fun getDewPoint(nwsConditions: NWSService.Conditions):String? {
        nwsConditions.dewPoint ?: return null
        val farenheit = toFahrenheit(nwsConditions.dewPoint!!)
        return "Dew Point %.0f℉".format(farenheit)
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
        val farenheit = toFahrenheit(nwsConditions.heatIndex!!)
        return "Heat Index %.0f℉".format(farenheit)
    }

}
