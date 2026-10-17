package com.brunodegan.androidplayground.data.datasources.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "popular")
data class PopularMoviesEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: Int = 0,
    @ColumnInfo(name = "title")
    override val title: String,
    @ColumnInfo(name = "posterPath")
    override val posterPath: String,
    @ColumnInfo(name = "overview")
    override val overview: String,
    @ColumnInfo(name = "original_language")
    override val originalLanguage: String,
    @ColumnInfo(name = "popularity")
    override val popularity: Double,
    @ColumnInfo(name = "vote_average")
    override val voteAverage: Int,
    @ColumnInfo(name = "release_date")
    override val releaseDate: String,
    @ColumnInfo(name = "is_favorite")
    override val isFavorite: Boolean,
) : MovieEntity

fun List<PopularMoviesEntity>.update(favoritesMovies: List<FavoriteMoviesEntity>): List<PopularMoviesEntity> =
    this.map { movie ->
        val isFavorite = favoritesMovies.any { it.id == movie.id }
        movie.copy(isFavorite = isFavorite)
    }
