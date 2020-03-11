package com.example.weather

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.Services.LocationServices
import com.example.weather.Services.NWSRefreshService
import com.example.weather.ui.main.ConditionsFragment

class MainActivity : AppCompatActivity() {
    private val tag = javaClass.kotlin.qualifiedName

    private lateinit var locationServices: LocationServices
    private lateinit var nwsRefreshService: NWSRefreshService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ConditionsFragment())
                .commitNow()
        }

        locationServices = LocationServices(this)
        nwsRefreshService = NWSRefreshService(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LocationServices.LOCATION_REQUEST -> {
                Log.d(tag, "got user permission")
                locationServices.onRequestPermissionsResult()
            }
            else -> {
                Log.d(tag, "failed to get user permission")
            }
        }
    }
}
