package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.example.weather.MainActivity.NWSService.nwsService
import com.example.weather.ui.main.ConditionsFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.kotlin.qualifiedName

    var LOCATION_REQUEST: Int = 1


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

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
    else
        requestLocationUpdates()
    }

    fun requestLocationUpdates() {
            val fusedLocationClient = getFusedLocationProviderClient(applicationContext)

        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    nwsService.setLocation(location)
                }
            }
        }

        val locationRequest = LocationRequest.create()?.apply {
            var interval = 15000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
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
                val nwsRefreshService = NWSRefreshService(this)
                nwsRefreshService.begin()
            }
            else -> {
                Log.d(TAG, "failed to get user permission")
            }
        }

        val refreshService = NWSRefreshService(this)
        refreshService.begin()
    }

}

class LocationService(var appContext: AppCompatActivity) {

        fun begin() {
            val constrains = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()

            val refreshWorkRequest = PeriodicWorkRequest.Builder(NWSRefreshService.Service::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constrains)
                .build()
            WorkManager.getInstance(appContext).enqueue(refreshWorkRequest)
//        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("refreshWorkRequest", ExistingPeriodicWorkPolicy.KEEP, refreshWorkRequest)
        }

    }

class LocationServices(var appContext: AppCompatActivity, workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams){
    private val TAG = javaClass.kotlin.qualifiedName

    override fun startWork(): ListenableFuture<Result> {
        Log.d(TAG, "get locatoin started")
        return CallbackToFutureAdapter.getFuture { resolver ->
            Log.d(TAG, "got device locatoin")
            GlobalScope.launch { nwsService.refresh() }
            resolver.set(Result.success())
        }
    }
}
