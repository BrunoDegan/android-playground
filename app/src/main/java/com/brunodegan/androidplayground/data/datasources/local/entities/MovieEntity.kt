package com.brunodegan.androidplayground.data.datasources.local.entities

interface MovieEntity {
    val id: Int
    val title: String
    val posterPath: String
    val overview: String
    val originalLanguage: String
    val popularity: Double
    val voteAverage: Int
    val releaseDate: String
    val isFavorite: Boolean
}
