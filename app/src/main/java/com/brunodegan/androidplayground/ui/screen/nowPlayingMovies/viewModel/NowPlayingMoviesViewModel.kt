package com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.viewModel

import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.ui.MovieListUiState
import com.brunodegan.androidplayground.base.ui.viewModel.CommonViewModel
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getNowPlaying.GetNowPlayingUseCase
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.actions.NowPlayingMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.state.NowPlayingMoviesUiState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class NowPlayingMoviesViewModel(
    useCase: GetNowPlayingUseCase,
    addToFavoritesUseCase: AddToFavoritesUseCase,
    removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    movieUiModelMapper: MovieUiModelMapper,
    errorUiModelMapper: ErrorUiModelMapper,
    dispatcher: DispatchersProviderInterface,
) : CommonViewModel<NowPlayingMoviesUiState>(
        loadMovies = { useCase() },
        states =
            MovieListUiState(
                initial = NowPlayingMoviesUiState.Initial,
                loading = NowPlayingMoviesUiState.Loading,
                success = NowPlayingMoviesUiState::Success,
                error = NowPlayingMoviesUiState::Error,
            ),
        movieUiModelMapper = movieUiModelMapper,
        errorUiModelMapper = errorUiModelMapper,
        addToFavoritesUseCase = addToFavoritesUseCase,
        removeFromFavoritesUseCase = removeFromFavoritesUseCase,
        dispatcher = dispatcher,
    ) {
    fun onUiAction(action: NowPlayingMoviesUiActions) {
        when (action) {
            is NowPlayingMoviesUiActions.OnRetryButtonClickedUiAction -> load()
            is NowPlayingMoviesUiActions.OnAddFavButtonClickedUiAction -> addFavorite(action.id)
            is NowPlayingMoviesUiActions.OnRemoveFavButtonClickedUiAction -> removeFavorite(action.id)
        }
    }
}
