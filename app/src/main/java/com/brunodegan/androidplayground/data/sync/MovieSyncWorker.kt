package com.brunodegan.androidplayground.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brunodegan.androidplayground.domain.repositories.MoviesRepository
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
        // Any category failing retries the whole run: saves are idempotent (table replace), so re-fetching is safe
        val anyFailed =
            try {
                repository.refreshAll().hasFetchingFailure
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                true
            }
        return if (anyFailed && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }
}
