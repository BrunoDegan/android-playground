package com.brunodegan.androidplayground.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit

const val SYNC_WORK_ID = "movie_sync"

@Single
class MovieSyncScheduler(
    private val context: Context,
) {
    fun schedule() {
        val request =
            PeriodicWorkRequestBuilder<MovieSyncWorker>(
                repeatInterval = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                repeatIntervalTimeUnit = TimeUnit.MILLISECONDS,
            ).setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build(),
            ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                SYNC_WORK_ID,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
    }
}
