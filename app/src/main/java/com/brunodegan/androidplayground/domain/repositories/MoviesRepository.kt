package com.brunodegan.androidplayground.domain.repositories

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.domain.model.FavoriteResult
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.model.SyncResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun getTopRateMovies(): Flow<Resource<ImmutableList<Movie>>>

    fun getPopularMovies(): Flow<Resource<ImmutableList<Movie>>>

    fun getUpcomingMovies(): Flow<Resource<ImmutableList<Movie>>>

    fun getNowPlayingMovies(): Flow<Resource<ImmutableList<Movie>>>

    fun addFavorite(id: Int): Flow<Resource<FavoriteResult>>

    fun removeFavorite(id: Int): Flow<Resource<FavoriteResult>>

    fun getFavorites(): Flow<Resource<ImmutableList<Movie>>>

    suspend fun refreshAll(): SyncResult
}
