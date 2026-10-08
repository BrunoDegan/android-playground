package com.brunodegan.androidplayground.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brunodegan.androidplayground.data.repositories.MoviesRepository
import kotlinx.coroutines.CancellationException
import org.koin.android.annotation.KoinWorker

private const val MAX_RETRIES = 3

@KoinWorker
class MovieSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: MoviesRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val allFailed =
            try {
                repository.refreshAll().allFailed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                true
            }
        return if (allFailed && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }
}
