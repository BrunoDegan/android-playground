package com.brunodegan.androidplayground.data.sync

enum class SyncCategory { NOW_PLAYING, POPULAR, TOP_RATED, UPCOMING, FAVORITES }

sealed interface SyncOutcome {
    data class Success(
        val count: Int,
    ) : SyncOutcome

    data class Failure(
        val message: String?,
    ) : SyncOutcome
}

data class SyncResult(
    val outcomes: Map<SyncCategory, SyncOutcome>,
) {
    val hasFetchingFailure: Boolean get() = outcomes.values.any { it is SyncOutcome.Failure }
}
