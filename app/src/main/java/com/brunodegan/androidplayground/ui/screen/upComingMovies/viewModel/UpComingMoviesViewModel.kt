package com.brunodegan.androidplayground.ui.screen.upComingMovies.viewModel

import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.ui.MovieListUiState
import com.brunodegan.androidplayground.base.ui.viewModel.CommonViewModel
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getUpComing.GetUpComingUseCase
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.screen.upComingMovies.actions.UpComingMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.upComingMovies.state.UpComingMoviesUiState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UpComingMoviesViewModel(
    useCase: GetUpComingUseCase,
    addToFavoritesUseCase: AddToFavoritesUseCase,
    removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    movieUiModelMapper: MovieUiModelMapper,
    errorUiModelMapper: ErrorUiModelMapper,
    dispatcher: DispatchersProviderInterface,
) : CommonViewModel<UpComingMoviesUiState>(
        loadMovies = { useCase() },
        states =
            MovieListUiState(
                initial = UpComingMoviesUiState.Initial,
                loading = UpComingMoviesUiState.Loading,
                success = UpComingMoviesUiState::Success,
                error = UpComingMoviesUiState::Error,
            ),
        movieUiModelMapper = movieUiModelMapper,
        errorUiModelMapper = errorUiModelMapper,
        addToFavoritesUseCase = addToFavoritesUseCase,
        removeFromFavoritesUseCase = removeFromFavoritesUseCase,
        dispatcher = dispatcher,
    ) {
    fun onUiAction(action: UpComingMoviesUiActions) {
        when (action) {
            is UpComingMoviesUiActions.OnRetryButtonClickedUiAction -> load()
            is UpComingMoviesUiActions.OnAddFavButtonClickedUiAction -> addFavorite(action.id)
            is UpComingMoviesUiActions.OnRemoveFavButtonClickedUiAction -> removeFavorite(action.id)
        }
    }
}
