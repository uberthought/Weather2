package com.example.weather.Services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weather.MainActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationServices(var appContext: AppCompatActivity) :
    ActivityCompat.OnRequestPermissionsResultCallback {
    private val TAG = javaClass.kotlin.qualifiedName

    var LOCATION_REQUEST: Int = 1

    fun begin() {
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                appContext,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST
            )
        else
            requestLocationUpdates()
    }

    fun requestLocationUpdates() {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(
                appContext
            )

        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    MainActivity.nwsService.setLocation(location)
                }
            }
        }

        val locationRequest = LocationRequest.create()
            ?.apply {
            var interval = 15000
            fastestInterval = 5000
            priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "callback from user permission")
        when (requestCode) {
            LOCATION_REQUEST -> {
                Log.d(TAG, "got user permission")
                val nwsRefreshService =
                    NWSRefreshService(appContext)
                nwsRefreshService.begin()
            }
            else -> {
                Log.d(TAG, "failed to get user permission")
            }
        }

        val refreshService =
            NWSRefreshService(appContext)
        refreshService.begin()
    }
}