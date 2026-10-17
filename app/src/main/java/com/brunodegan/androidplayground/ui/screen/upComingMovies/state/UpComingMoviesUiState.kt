package com.brunodegan.androidplayground.ui.screen.upComingMovies.state

import androidx.compose.runtime.Immutable
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface UpComingMoviesUiState {
    data object Initial : UpComingMoviesUiState

    data object Loading : UpComingMoviesUiState

    data class Success(
        val viewData: ImmutableList<MovieUiModel>,
    ) : UpComingMoviesUiState

    data class Error(
        val error: ErrorUiModel,
    ) : UpComingMoviesUiState
}
