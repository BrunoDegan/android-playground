package com.brunodegan.androidplayground.ui.screen.popularMovies.viewModel

import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.ui.MovieListUiState
import com.brunodegan.androidplayground.base.ui.viewModel.CommonViewModel
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getPopular.GetPopularUseCase
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.screen.popularMovies.actions.PopularMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.popularMovies.state.PopularMoviesUiState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PopularMoviesViewModel(
    useCase: GetPopularUseCase,
    addToFavoritesUseCase: AddToFavoritesUseCase,
    removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    movieUiModelMapper: MovieUiModelMapper,
    errorUiModelMapper: ErrorUiModelMapper,
    dispatcher: DispatchersProviderInterface,
) : CommonViewModel<PopularMoviesUiState>(
        loadMovies = { useCase() },
        states =
            MovieListUiState(
                initial = PopularMoviesUiState.Initial,
                loading = PopularMoviesUiState.Loading,
                success = PopularMoviesUiState::Success,
                error = PopularMoviesUiState::Error,
            ),
        movieUiModelMapper = movieUiModelMapper,
        errorUiModelMapper = errorUiModelMapper,
        addToFavoritesUseCase = addToFavoritesUseCase,
        removeFromFavoritesUseCase = removeFromFavoritesUseCase,
        dispatcher = dispatcher,
    ) {
    fun onUiAction(action: PopularMoviesUiActions) {
        when (action) {
            is PopularMoviesUiActions.OnRetryButtonClickedUiAction -> load()
            is PopularMoviesUiActions.OnAddFavButtonClickedUiAction -> addFavorite(action.id)
            is PopularMoviesUiActions.OnRemoveFavButtonClickedUiAction -> removeFavorite(action.id)
        }
    }
}
