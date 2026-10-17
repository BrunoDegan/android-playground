package com.brunodegan.androidplayground.ui.screen.topRatedMovies.viewModel

import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.ui.MovieListUiState
import com.brunodegan.androidplayground.base.ui.viewModel.CommonViewModel
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getTopRated.GetTopRatedUseCase
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.actions.TopRatedMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.state.TopRatedMoviesUiState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TopRatedMoviesViewModel(
    useCase: GetTopRatedUseCase,
    addToFavoritesUseCase: AddToFavoritesUseCase,
    removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    movieUiModelMapper: MovieUiModelMapper,
    errorUiModelMapper: ErrorUiModelMapper,
    dispatcher: DispatchersProviderInterface,
) : CommonViewModel<TopRatedMoviesUiState>(
        loadMovies = { useCase() },
        states =
            MovieListUiState(
                initial = TopRatedMoviesUiState.Initial,
                loading = TopRatedMoviesUiState.Loading,
                success = TopRatedMoviesUiState::Success,
                error = TopRatedMoviesUiState::Error,
            ),
        movieUiModelMapper = movieUiModelMapper,
        errorUiModelMapper = errorUiModelMapper,
        addToFavoritesUseCase = addToFavoritesUseCase,
        removeFromFavoritesUseCase = removeFromFavoritesUseCase,
        dispatcher = dispatcher,
    ) {
    fun onUiAction(action: TopRatedMoviesUiActions) {
        when (action) {
            is TopRatedMoviesUiActions.OnRetryButtonClickedUiAction -> load()
            is TopRatedMoviesUiActions.OnAddFavButtonClickedUiAction -> addFavorite(action.id)
            is TopRatedMoviesUiActions.OnRemoveFavButtonClickedUiAction -> removeFavorite(action.id)
        }
    }
}
