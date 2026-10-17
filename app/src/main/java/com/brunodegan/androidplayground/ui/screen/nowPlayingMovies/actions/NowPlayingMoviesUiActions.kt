package com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.actions

sealed interface NowPlayingMoviesUiActions {
    data class OnAddFavButtonClickedUiAction(
        val id: Int,
    ) : NowPlayingMoviesUiActions

    data class OnRemoveFavButtonClickedUiAction(
        val id: Int,
    ) : NowPlayingMoviesUiActions

    data object OnRetryButtonClickedUiAction : NowPlayingMoviesUiActions
}
