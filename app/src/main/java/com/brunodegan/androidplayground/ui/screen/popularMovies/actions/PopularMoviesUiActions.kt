package com.brunodegan.androidplayground.ui.screen.popularMovies.actions

sealed interface PopularMoviesUiActions {
    data class OnRemoveFavButtonClickedUiAction(
        val id: Int,
    ) : PopularMoviesUiActions

    data class OnAddFavButtonClickedUiAction(
        val id: Int,
    ) : PopularMoviesUiActions

    data object OnRetryButtonClickedUiAction : PopularMoviesUiActions
}
