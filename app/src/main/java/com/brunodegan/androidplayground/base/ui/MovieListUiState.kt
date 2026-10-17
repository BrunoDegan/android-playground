package com.brunodegan.androidplayground.base.ui

import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList

class MovieListUiState<S>(
    val initial: S,
    val loading: S,
    val success: (ImmutableList<MovieUiModel>) -> S,
    val error: (ErrorUiModel) -> S,
)
