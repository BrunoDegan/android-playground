package com.brunodegan.androidplayground.ui.screen.topRatedMovies.state

import androidx.compose.runtime.Immutable
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface TopRatedMoviesUiState {
    data object Initial : TopRatedMoviesUiState

    data object Loading : TopRatedMoviesUiState

    data class Success(
        val viewData: ImmutableList<MovieUiModel>,
    ) : TopRatedMoviesUiState

    data class Error(
        val error: ErrorUiModel,
    ) : TopRatedMoviesUiState
}
