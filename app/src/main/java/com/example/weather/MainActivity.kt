package com.example.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.example.weather.Services.NWSService
import com.example.weather.ui.main.ConditionsFragment
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val tag = javaClass.kotlin.qualifiedName

    companion object {
        const val LOCATION_REQUEST: Int = 1
    }

    private lateinit var fusedLocationClient:FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ConditionsFragment())
                .commitNow()
        }

        // location

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
        else
            requestLocationUpdates()

        Log.d(tag, "setup constraints so we don't hammer the device")
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        // refresh timer

        Log.d(tag, "request refresh every ${RefreshService.refreshInterval} minutes")
        val refreshWorkRequest = PeriodicWorkRequest.Builder(RefreshService::class.java, RefreshService.refreshInterval, TimeUnit.MINUTES)
            .setConstraints(constrains)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(refreshWorkRequest)
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
                interval = 1000 * 60 * 5
                fastestInterval = 1000 * 60 * 15
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                Log.d(tag, "got user permission")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    NWSService.instance.setLocation(location)
                }
            }
            else -> {
                Log.d(tag, "failed to get user permission")
            }
        }
    }

    class RefreshService(appContext: Context, workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams) {
        private val tag = javaClass.kotlin.qualifiedName

        companion object {
            val mutex = Mutex()
            var refreshInterval:Long = 15
            var timestamp: Long = 0
        }

        override fun startWork(): ListenableFuture<Result> {
            return CallbackToFutureAdapter.getFuture { resolver ->
                GlobalScope.launch {
                    mutex.withLock {
                        val duration = Date().time - timestamp
                        if (duration > 1000 * 60 * 5) {
                            timestamp = Date().time
                            Log.d(tag, "refresh NWS after $duration")
                            GlobalScope.launch { NWSService.instance.refresh() }
                        } else
                            Log.d(tag, "trying to refresh NWS too soon $duration")

                        resolver.set(Result.success())
                    }
                }
            }
        }
    }
}
