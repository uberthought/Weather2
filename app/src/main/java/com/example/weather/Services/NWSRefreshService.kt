package com.example.weather.Services

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.*
import com.example.weather.MainActivity
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.TimeUnit

class NWSRefreshService(var appContext: AppCompatActivity) {
    private val TAG = javaClass.kotlin.qualifiedName

    companion object Mutex {
        val mutex = Mutex()
        var refreshInterval:Long = 15
        var timestamp: Long = 0
    }

    fun begin() {
        Log.d(TAG, "setup constraints so we don't hammer the device")
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        Log.d(TAG, "request refresh every $refreshInterval minutes")
        val refreshWorkRequest =
            PeriodicWorkRequest.Builder(
                    Service::class.java,
                    refreshInterval, TimeUnit.MINUTES)
                .setConstraints(constrains)
                .build()
        WorkManager.getInstance(appContext).enqueue(refreshWorkRequest)
    }

    class Service(appContext: Context, workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams) {
        private val TAG = javaClass.kotlin.qualifiedName

        override fun startWork(): ListenableFuture<Result> {
            return CallbackToFutureAdapter.getFuture { resolver ->
                GlobalScope.launch {
                    mutex.withLock {
                        val duration = Date().time - timestamp
                        if (duration > 1000 * 60 * 5) {
                            timestamp = Date().time
                            Log.d(TAG, "refresh NWS after $duration")
                            MainActivity.nwsService.refresh()
                        } else
                            Log.d(TAG, "trying to refresh NWS too soon $duration")

                        resolver.set(Result.success())
                    }
                }
            }
        }
    }
}

