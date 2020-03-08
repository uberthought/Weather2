package com.example.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.example.weather.ui.main.ConditionsFragment
import com.google.android.gms.location.*
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    var LOCATION_REQUEST:Int = 1

    companion object NWSService { var nwsService = NWSService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ConditionsFragment())
                .commitNow()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST)
        else
            requestLocationUpdates()

        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        val refreshWorkRequest = PeriodicWorkRequest.Builder(RefreshNWSService::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(constrains)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(refreshWorkRequest)
//        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("refreshWorkRequest", ExistingPeriodicWorkPolicy.KEEP, refreshWorkRequest)
    }

    class RefreshNWSService(appContext: Context,workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams) {
        override fun startWork(): ListenableFuture<Result> {
            return CallbackToFutureAdapter.getFuture { resolver ->
                GlobalScope.launch { nwsService.refresh() }
                resolver.set(Result.success())
            }
        }
    }

    fun requestLocationUpdates() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    nwsService.setLocation(location)
                }
            }
        }

        val locationRequest = LocationRequest.create()?.apply {
            interval = 15000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            LOCATION_REQUEST -> {
                requestLocationUpdates()
                println("OK")
            }
            else -> {
                println("NOT OK")
            }
        }
    }
}
