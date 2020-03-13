package com.example.weather.Services

import NWSPoints
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class NWSService {
    private val logTag = javaClass.kotlin.qualifiedName

    companion object Mutex {
        val instance = NWSService()

        val mutex = Mutex()
        const val refreshInterval:Long = 15
        var timestamp: Long = 0
    }

    private var stationId: String? = null
    private var gridX: Int? = null
    private var gridY: Int? = null
    private var gridWFO: String? = null
    private var lastLocation:Location? = null
    private var baseURL = "https://api.weather.gov"

    class Conditions {
        var dewPoint: Double? = null
        var relativeHumidity: Double? = null
        var temperature: Double? = null
        var windDirection: Double? = null
        var windSpeed: Double? = null
        var windGust: Double? = null
        var icon:String? = null
        var textDescription:String? = null
        var barometricPressure:Double? = null
        var visibility:Double? = null
        var windChill:Double? = null
        var heatIndex:Double? = null
        var timestamp:Date? = null
    }

    class Forecast {
        var names: MutableList<String> = mutableListOf()
        var icons: MutableList<String> = mutableListOf()
        var shortForecasts: MutableList<String> = mutableListOf()
        var detailedForecasts: MutableList<String> = mutableListOf()
        var temperatures: MutableList<String> = mutableListOf()
        var windSpeeds: MutableList<String> = mutableListOf()
        var windDirections: MutableList<String> = mutableListOf()
    }

    var location: MutableLiveData<String> = MutableLiveData()
    var conditions: MutableLiveData<Conditions> = MutableLiveData()
    var forecast: MutableLiveData<Forecast> = MutableLiveData()

    fun setLocation(location: Location) {
        Log.d(logTag, "got update from location service")
        if (lastLocation == null || lastLocation!!.distanceTo(location) > 100  ) {
            Log.d(logTag, "refreshing since the location changes by greater than 100m")
            lastLocation = Location(location)
            stationId = null
            timestamp = 0
            GlobalScope.launch { refresh() }
        }
        else
            Log.d(logTag, "not refreshing since the location changes bt at less than 100m")

    }

    suspend fun refresh() {
        Log.d(logTag, "starting NWS refresh")

        mutex.withLock {
            val duration = Date().time - timestamp
            if (duration > 1000 * 60 * refreshInterval) {
                timestamp = Date().time
                if ((lastLocation != null)) {
                    Log.d(logTag, "starting NWS refresh")
                    getStation()
                    getConditions()
                    getForecast()
                }
                else
                    Log.d(logTag, "skipping refresh because location isn't set")
            }
            else
                Log.d(logTag, "skipping refresh because refreshing too soon")
        }
    }

    private fun getStation() {
        val latitude = lastLocation!!.latitude
        val longitude = lastLocation!!.longitude
        val url = URL("$baseURL/points/$latitude,$longitude/stations")
        val response = url.readText()
        val points = Gson().fromJson(response, NWSPoints.Base::class.java)
        location.postValue(points.features[0].properties.name)
        stationId = points.features[0].properties.stationIdentifier
    }

    @SuppressLint("SimpleDateFormat")
    private fun getConditions() {
        val url = URL("$baseURL/stations/$stationId/observations/latest?require_qc=false")
        val response = url.readText()
        val obj = JSONObject(response)
        val properties = obj.getJSONObject("properties")

        val conditions = Conditions()

        conditions.dewPoint = properties.getJSONObject("dewpoint").getString("value").toDoubleOrNull()
        conditions.relativeHumidity = properties.getJSONObject("relativeHumidity").getString("value").toDoubleOrNull()
        conditions.temperature = properties.getJSONObject("temperature").getString("value").toDoubleOrNull()
        conditions.windDirection = properties.getJSONObject("windDirection").getString("value").toDoubleOrNull()
        conditions.windSpeed = properties.getJSONObject("windSpeed").getString("value").toDoubleOrNull()
        conditions.windGust = properties.getJSONObject("windGust").getString("value").toDoubleOrNull()
        conditions.icon = properties.getString("icon")
        conditions.textDescription = properties.getString("textDescription")
        conditions.barometricPressure = properties.getJSONObject("barometricPressure").getString("value").toDoubleOrNull()
        conditions.visibility = properties.getJSONObject("visibility").getString("value").toDoubleOrNull()
        conditions.windChill = properties.getJSONObject("windChill").getString("value").toDoubleOrNull()
        conditions.heatIndex = properties.getJSONObject("heatIndex").getString("value").toDoubleOrNull()

        val timestamp = properties.getString("timestamp")
        if (timestamp.isNotEmpty())
            conditions.timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzzzz").parse(timestamp)

        this.conditions.postValue(conditions)
    }

    private fun getForecast() {
        if (gridX == null || gridY == null || gridWFO == null)
            getGridPoint()

        val url = URL("$baseURL/gridpoints/$gridWFO/$gridX,$gridY/forecast")
        val response = url.readText()
        val obj = JSONObject(response)
        val properties = obj.getJSONObject("properties")

        val forecast = Forecast()

        val periods = properties.getJSONArray("periods")
        for (i in 0 until periods.length()) {
            val period = periods.getJSONObject(i)

            forecast.names.add(period.getString("name"))
            forecast.icons.add(period.getString("icon"))
            forecast.shortForecasts.add(period.getString("shortForecast"))
            forecast.detailedForecasts.add(period.getString("detailedForecast"))
            forecast.temperatures.add(period.getString("temperature"))
            forecast.windSpeeds.add(period.getString("windSpeed"))
            forecast.windDirections.add(period.getString("windDirection"))
        }

        this.forecast.postValue(forecast)
    }

    private fun getGridPoint() {
        val latitude = lastLocation!!.latitude
        val longitude = lastLocation!!.longitude
        val url = URL("$baseURL/points/$latitude,$longitude")
        val response = url.readText()
        val obj = JSONObject(response)
        val properties = obj.getJSONObject("properties")

        gridWFO = properties.getString("cwa")
        gridX = properties.getString("gridX").toIntOrNull()
        gridY = properties.getString("gridY").toIntOrNull()
    }
}