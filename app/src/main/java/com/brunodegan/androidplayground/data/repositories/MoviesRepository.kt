package com.brunodegan.androidplayground.data.repositories

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoriteMoviesData
import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import com.brunodegan.androidplayground.data.sync.SyncResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun getTopRateMovies(): Flow<Resource<ImmutableList<TopRatedMoviesEntity>>>

    fun getPopularMovies(): Flow<Resource<ImmutableList<PopularMoviesEntity>>>

    fun getUpcomingMovies(): Flow<Resource<ImmutableList<UpcomingMoviesEntity>>>

    fun getNowPlayingMovies(): Flow<Resource<ImmutableList<NowPlayingMoviesEntity>>>

    fun addFavorite(id: Int): Flow<Resource<AddToFavoriteMoviesData>>

    fun removeFavorite(id: Int): Flow<Resource<AddToFavoriteMoviesData>>

    fun getFavorites(): Flow<Resource<ImmutableList<FavoriteMoviesEntity>>>

    suspend fun refreshAll(): SyncResult
}
