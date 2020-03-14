package com.example.weather.ui.main

import androidx.lifecycle.ViewModel
import com.example.weather.R

class DataViewModel(
    // general
    val title:String? = null,
    val shortDescription:String? = null,
    val detailedDescription:String? = null,
    val temperatureLabel:String? = null,
    val temperature:String? = null,
    val wind:String? = null,
    val icon:String? = null,
    val backgroundColor:Int = R.color.primaryColor,

    // conditions
    val dewPoint:String? = null,
    val relativeHumidity:String? = null,
    val windGust:String? = null,
    val visibility:String? = null,
    val pressure:String? = null,
    val windChill:String? = null,
    val heatIndex:String? = null,

    // forecast
    val temperatureTrend:String? = null
) : ViewModel()