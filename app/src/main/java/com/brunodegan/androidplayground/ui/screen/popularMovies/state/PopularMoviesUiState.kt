package com.brunodegan.androidplayground.ui.screen.popularMovies.state

import androidx.compose.runtime.Immutable
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface PopularMoviesUiState {
    data object Initial : PopularMoviesUiState

    data object Loading : PopularMoviesUiState

    data class Success(
        val viewData: ImmutableList<MovieUiModel>,
    ) : PopularMoviesUiState

    data class Error(
        val error: ErrorUiModel,
    ) : PopularMoviesUiState
}
