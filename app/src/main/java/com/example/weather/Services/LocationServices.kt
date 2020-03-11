package com.example.weather.Services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationServices(private var appContext: AppCompatActivity) {
    private val tag = javaClass.kotlin.qualifiedName

    companion object {
        const val LOCATION_REQUEST: Int = 1
    }

    init {
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(appContext, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST)
        else
            requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    NWSService.instance.setLocation(location)
                }
            }
        }

        val locationRequest = LocationRequest.create()
            ?.apply {
            interval = 15000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun onRequestPermissionsResult() {
        Log.d(tag, "got user permission")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            NWSService.instance.setLocation(location)
        }
    }
}