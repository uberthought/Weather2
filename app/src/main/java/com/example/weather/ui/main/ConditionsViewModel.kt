package com.example.weather.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.MainActivity
import com.example.weather.NWSService
import java.text.SimpleDateFormat

class ConditionsViewModel : ViewModel() {
    val timestamp: MutableLiveData<String> = MutableLiveData()
    val dataViewModel: MutableLiveData<DataViewModel> = MutableLiveData()

    init {
        MainActivity.nwsService.conditions.observeForever { conditions -> onConditionsChanged(conditions) }
    }

    private fun onConditionsChanged(nwsConditions: NWSService.Conditions) {
        val data = DataViewModel()
        with(data) {
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
        this.dataViewModel.postValue(data)
        this.timestamp.postValue(getTimestamp(nwsConditions))
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
