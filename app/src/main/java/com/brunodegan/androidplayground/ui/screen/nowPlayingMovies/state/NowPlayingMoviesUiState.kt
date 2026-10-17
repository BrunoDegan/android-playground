package com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.state

import androidx.compose.runtime.Immutable
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface NowPlayingMoviesUiState {
    data object Initial : NowPlayingMoviesUiState

    data object Loading : NowPlayingMoviesUiState

    data class Success(
        val viewData: ImmutableList<MovieUiModel>,
    ) : NowPlayingMoviesUiState

    data class Error(
        val error: ErrorUiModel,
    ) : NowPlayingMoviesUiState
}
