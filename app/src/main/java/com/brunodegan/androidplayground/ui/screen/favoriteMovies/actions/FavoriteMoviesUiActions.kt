package com.brunodegan.androidplayground.ui.screen.favoriteMovies.actions

sealed interface FavoriteMoviesUiActions {
    data object OnRetryButtonClickedUiAction : FavoriteMoviesUiActions
}
