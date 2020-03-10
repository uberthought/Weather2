package com.example.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weather.Services.LocationServices
import com.example.weather.Services.NWSRefreshService
import com.example.weather.Services.NWSService
import com.example.weather.ui.main.ConditionsFragment

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.kotlin.qualifiedName

    companion object NWSService {
        var nwsService = NWSService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ConditionsFragment())
                .commitNow()
        }

        val locationServices =
            LocationServices(this)
        locationServices.begin()

        val nwsRefreshService =
            NWSRefreshService(this)
        nwsRefreshService.begin()
    }

}
