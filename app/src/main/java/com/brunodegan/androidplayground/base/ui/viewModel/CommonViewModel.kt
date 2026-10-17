package com.brunodegan.androidplayground.base.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.ui.BaseViewViewModel
import com.brunodegan.androidplayground.base.ui.MovieListUiState
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

abstract class CommonViewModel<S : Any>(
    loadMovies: suspend () -> Flow<Resource<ImmutableList<Movie>>>,
    states: MovieListUiState<S>,
    movieUiModelMapper: MovieUiModelMapper,
    errorUiModelMapper: ErrorUiModelMapper,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    dispatcher: DispatchersProviderInterface,
) : BaseViewViewModel<S>(loadMovies, states, movieUiModelMapper, errorUiModelMapper, dispatcher) {
    protected fun addFavorite(movieId: Int) {
        viewModelScope.launch {
            addToFavoritesUseCase(movieId)
                .flowOn(dispatcher.io)
                .catch { showSnackbar(it.message) }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> showSnackbar(result.data.statusMessage)
                        is Resource.Error -> showSnackbar(result.error.message)
                    }
                }
        }
    }

    protected fun removeFavorite(movieId: Int) {
        viewModelScope.launch {
            removeFromFavoritesUseCase(movieId)
                .flowOn(dispatcher.io)
                .catch { showSnackbar(it.message) }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> if (result.data.success) showSnackbar(result.data.statusMessage)
                        is Resource.Error -> showSnackbar(result.error.message)
                    }
                }
        }
    }
}
