package com.brunodegan.androidplayground.ui.screen.upComingMovies.actions

sealed interface UpComingMoviesUiActions {
    data object OnRetryButtonClickedUiAction : UpComingMoviesUiActions

    data class OnAddFavButtonClickedUiAction(
        val id: Int,
    ) : UpComingMoviesUiActions

    data class OnRemoveFavButtonClickedUiAction(
        val id: Int,
    ) : UpComingMoviesUiActions
}
