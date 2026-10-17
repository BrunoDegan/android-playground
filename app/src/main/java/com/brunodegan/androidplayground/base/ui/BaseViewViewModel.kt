package com.brunodegan.androidplayground.base.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunodegan.androidplayground.base.dispatchers.DispatchersProviderInterface
import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewViewModel<S : Any>(
    private val loadMovies: suspend () -> Flow<Resource<ImmutableList<Movie>>>,
    private val states: MovieListUiState<S>,
    private val movieUiModelMapper: MovieUiModelMapper,
    private val errorUiModelMapper: ErrorUiModelMapper,
    protected val dispatcher: DispatchersProviderInterface,
) : ViewModel() {
    private val _snackbarState = Channel<SnackbarUiStateHolder>()
    private var loadJob: Job? = null
    val snackbarState = _snackbarState.receiveAsFlow()
    private val _uiState = MutableStateFlow(states.initial)

    val uiState: StateFlow<S> =
        _uiState
            .onStart { load() }
            .onCompletion { unload() }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = states.initial,
            )

    protected fun unload() = run { loadJob?.takeIf { it.isActive }?.cancel() }

    protected fun load() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                loadMovies()
                    .flowOn(dispatcher.io)
                    .onStart { _uiState.update { states.loading } }
                    .catch { error ->
                        _uiState.update { states.error(errorUiModelMapper.map(ErrorType.Generic(error.message))) }
                        showSnackbar(error.message)
                    }.collect { result ->
                        _uiState.update {
                            when (result) {
                                is Resource.Success -> states.success(result.data.map { movieUiModelMapper.map(it) }.toImmutableList())
                                is Resource.Error -> states.error(errorUiModelMapper.map(result.error))
                            }
                        }
                    }
            }
    }

    protected suspend fun showSnackbar(message: String?) {
        message?.let { _snackbarState.send(SnackbarUiStateHolder.SnackbarUi(it)) }
    }
}
