package com.brunodegan.androidplayground.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class MovieUiModel(
    val id: Int,
    val title: String,
    val posterPath: String,
    val overview: String,
    val releaseDate: String,
    val originalLanguage: String = "",
    val popularity: Double = 0.0,
    val voteAverage: Int = 0,
    val isFavorite: Boolean = false,
)
