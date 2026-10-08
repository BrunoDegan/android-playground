package com.brunodegan.androidplayground.data.datasources.data.sync

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.brunodegan.androidplayground.data.repositories.MoviesRepository
import com.brunodegan.androidplayground.data.sync.MovieSyncWorker
import com.brunodegan.androidplayground.data.sync.SyncCategory
import com.brunodegan.androidplayground.data.sync.SyncOutcome
import com.brunodegan.androidplayground.data.sync.SyncResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieSyncWorkerTest {
    private val repository: MoviesRepository = mockk()

    private fun worker(attempt: Int): MovieSyncWorker {
        val params = mockk<WorkerParameters>()
        every { params.runAttemptCount } returns attempt
        return MovieSyncWorker(mockk<Context>(), params, repository)
    }

    private val success = SyncResult(SyncCategory.entries.associateWith { SyncOutcome.Success(1) })
    private val failed = SyncResult(SyncCategory.entries.associateWith { SyncOutcome.Failure("x") })
    private val partial =
        SyncResult(
            mapOf(
                SyncCategory.POPULAR to SyncOutcome.Failure("x"),
                SyncCategory.TOP_RATED to SyncOutcome.Success(1),
            ),
        )

    @Test
    fun `GIVEN success WHEN doWork THEN success`() =
        runTest {
            coEvery { repository.refreshAll() } returns success
            assertEquals(Result.success(), worker(0).doWork())
        }

    @Test
    fun `GIVEN partial failure WHEN doWork THEN success`() =
        runTest {
            coEvery { repository.refreshAll() } returns partial
            assertEquals(Result.success(), worker(0).doWork())
        }

    @Test
    fun `GIVEN all failed on first attempt WHEN doWork THEN retry`() =
        runTest {
            coEvery { repository.refreshAll() } returns failed
            assertEquals(Result.retry(), worker(0).doWork())
        }

    @Test
    fun `GIVEN all failed after max attempts WHEN doWork THEN success to wait for next period`() =
        runTest {
            coEvery { repository.refreshAll() } returns failed
            assertEquals(Result.success(), worker(3).doWork())
        }

    @Test
    fun `GIVEN repository throws WHEN doWork THEN retry`() =
        runTest {
            coEvery { repository.refreshAll() } throws IllegalStateException("boom")
            assertEquals(Result.retry(), worker(0).doWork())
        }
}
