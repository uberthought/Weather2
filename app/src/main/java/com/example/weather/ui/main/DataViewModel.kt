package com.example.weather.ui.main

import androidx.lifecycle.ViewModel
import com.example.weather.R

class DataViewModel : ViewModel() {
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
    var backgroundColor:Int = R.color.primaryColor
}