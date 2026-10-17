package com.brunodegan.androidplayground.ui.screen.topRatedMovies.actions

sealed interface TopRatedMoviesUiActions {
    data class OnRemoveFavButtonClickedUiAction(
        val id: Int,
    ) : TopRatedMoviesUiActions

    data class OnAddFavButtonClickedUiAction(
        val id: Int,
    ) : TopRatedMoviesUiActions

    data object OnRetryButtonClickedUiAction : TopRatedMoviesUiActions
}
