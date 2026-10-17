package com.brunodegan.androidplayground.ui.screen.favoriteMovies.viewModel

import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.ui.BaseViewViewModel
import com.brunodegan.androidplayground.base.ui.MovieListUiState
import com.brunodegan.androidplayground.domain.getFavorites.GetFavoritesUseCase
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.actions.FavoriteMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.state.FavoriteMoviesUiState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class FavoritesViewModel(
    useCase: GetFavoritesUseCase,
    movieUiModelMapper: MovieUiModelMapper,
    errorUiModelMapper: ErrorUiModelMapper,
    dispatcher: DispatchersProviderInterface,
) : BaseViewViewModel<FavoriteMoviesUiState>(
        loadMovies = { useCase() },
        states =
            MovieListUiState(
                initial = FavoriteMoviesUiState.Initial,
                loading = FavoriteMoviesUiState.Loading,
                success = FavoriteMoviesUiState::Success,
                error = FavoriteMoviesUiState::Error,
            ),
        movieUiModelMapper = movieUiModelMapper,
        errorUiModelMapper = errorUiModelMapper,
        dispatcher = dispatcher,
    ) {
    fun onUiAction(action: FavoriteMoviesUiActions) {
        when (action) {
            is FavoriteMoviesUiActions.OnRetryButtonClickedUiAction -> load()
        }
    }
}
