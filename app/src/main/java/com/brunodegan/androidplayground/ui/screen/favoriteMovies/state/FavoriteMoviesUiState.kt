package com.brunodegan.androidplayground.ui.screen.favoriteMovies.state

import androidx.compose.runtime.Immutable
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface FavoriteMoviesUiState {
    data object Initial : FavoriteMoviesUiState

    data object Loading : FavoriteMoviesUiState

    data class Success(
        val viewData: ImmutableList<MovieUiModel>,
    ) : FavoriteMoviesUiState

    data class Error(
        val error: ErrorUiModel,
    ) : FavoriteMoviesUiState
}
