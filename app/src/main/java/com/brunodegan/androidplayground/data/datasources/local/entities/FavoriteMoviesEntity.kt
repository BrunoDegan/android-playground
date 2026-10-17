package com.brunodegan.androidplayground.data.datasources.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteMoviesEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: Int = 0,
    @ColumnInfo(name = "lastUpdated")
    val lastUpdated: Long,
    @ColumnInfo(name = "title")
    override val title: String,
    @ColumnInfo(name = "posterPath")
    override val posterPath: String,
    @ColumnInfo(name = "overview")
    override val overview: String,
    @ColumnInfo(name = "release_date")
    override val releaseDate: String,
) : MovieEntity {
    // Not stored for favorites; getters have no backing field so Room ignores them
    override val originalLanguage: String get() = ""
    override val popularity: Double get() = 0.0
    override val voteAverage: Int get() = 0
    override val isFavorite: Boolean get() = true
}
