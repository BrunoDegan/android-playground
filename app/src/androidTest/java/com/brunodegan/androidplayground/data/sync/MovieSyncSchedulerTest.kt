package com.brunodegan.androidplayground.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieSyncSchedulerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
    }

    @Test
    fun scheduleTwiceKeepsSingleEnqueuedPeriodicWork() {
        val scheduler = MovieSyncScheduler(context)

        scheduler.schedule()
        scheduler.schedule()

        val infos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
        assertEquals(NetworkType.CONNECTED, infos.single().constraints.requiredNetworkType)
        assertEquals(true, infos.single().constraints.requiresBatteryNotLow())
        assertEquals(true, infos.single().constraints.requiresStorageNotLow())
    }
}
