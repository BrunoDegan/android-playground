package com.brunodegan.androidplayground.data.repositories

import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.utils.isCacheValid
import com.brunodegan.androidplayground.data.datasources.local.LocalDataSource
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoriteMoviesData
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesRequest
import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.update
import com.brunodegan.androidplayground.data.datasources.remote.RemoteDataSource
import com.brunodegan.androidplayground.data.mappers.AddOrRemoveToFavoritesResponseDataMapper
import com.brunodegan.androidplayground.data.mappers.FavoritesDataMapper
import com.brunodegan.androidplayground.data.mappers.NowPlayingDataMapper
import com.brunodegan.androidplayground.data.mappers.PopularDataMapper
import com.brunodegan.androidplayground.data.mappers.TopRatedDataMapper
import com.brunodegan.androidplayground.data.mappers.UpcomingDataMapper
import com.brunodegan.androidplayground.data.metrics.Metrics
import com.brunodegan.androidplayground.data.sync.SyncCategory
import com.brunodegan.androidplayground.data.sync.SyncOutcome
import com.brunodegan.androidplayground.data.sync.SyncResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory

@Factory
class MoviesRepositoryImpl(
    private val addOrRemoveToFavoritesResponseDataMapper: AddOrRemoveToFavoritesResponseDataMapper,
    private val favoritesDataMapper: FavoritesDataMapper,
    private val nowPlayingMoviesDataMapper: NowPlayingDataMapper,
    private val popularMoviesDataMapper: PopularDataMapper,
    private val topRatedMoviesDataMapper: TopRatedDataMapper,
    private val upcomingMoviesDataMapper: UpcomingDataMapper,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val metricsEventsDispatcher: Metrics,
) : MoviesRepository {
    private fun updateTopRatedMovies(
        movies: List<TopRatedMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<TopRatedMoviesEntity> = (favorites?.let { movies.update(it) } ?: movies).toImmutableList()

    private fun updatePopularMovies(
        movies: List<PopularMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<PopularMoviesEntity> = (favorites?.let { movies.update(it) } ?: movies).toImmutableList()

    private fun updateUpcomingMovies(
        movies: List<UpcomingMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<UpcomingMoviesEntity> = (favorites?.let { movies.update(it) } ?: movies).toImmutableList()

    private fun updateNowPlayingMovies(
        movies: List<NowPlayingMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<NowPlayingMoviesEntity> = (favorites?.let { movies.update(it) } ?: movies).toImmutableList()

    override fun getTopRateMovies(): Flow<Resource<ImmutableList<TopRatedMoviesEntity>>> =
        flow {
            val localTopRatedMovies = localDataSource.getTopRated().firstOrNull()
            val savedFavoriteMovies = getSavedFavoriteMovies().firstOrNull()

            if (!localTopRatedMovies.isNullOrEmpty()) {
                emit(
                    Resource.Success(
                        updateTopRatedMovies(
                            localTopRatedMovies,
                            savedFavoriteMovies,
                        ),
                    ),
                )
            } else {
                runCatching {
                    remoteDataSource.fetchTopRated()
                }.onFailure { failure ->
                    failure.message
                        .takeIf { it.isNullOrEmpty().not() }
                        ?.let { metricsEventsDispatcher.onEvent(it) }
                }.map { apiDataModel ->
                    val mappedResult = topRatedMoviesDataMapper.map(apiDataModel)
                    localDataSource.saveTopRated(mappedResult)
                    mappedResult
                }.onSuccess { convertedData ->
                    emit(
                        Resource.Success(
                            updateTopRatedMovies(
                                convertedData,
                                savedFavoriteMovies,
                            ),
                        ),
                    )
                }.recoverCatching { error ->
                    emit(Resource.Error(ErrorType.Generic(error.message)))
                }
            }
        }

    override fun getPopularMovies(): Flow<Resource<ImmutableList<PopularMoviesEntity>>> =
        flow {
            val localPopularMovies = localDataSource.getPopular().firstOrNull()
            val savedFavoriteMovies = getSavedFavoriteMovies().firstOrNull()

            if (!localPopularMovies.isNullOrEmpty()) {
                emit(
                    Resource.Success(
                        updatePopularMovies(
                            localPopularMovies,
                            savedFavoriteMovies,
                        ),
                    ),
                )
            } else {
                runCatching {
                    remoteDataSource.fetchPopular()
                }.onFailure { failure ->
                    failure.message
                        .takeIf { it.isNullOrEmpty().not() }
                        ?.let { metricsEventsDispatcher.onEvent(it) }
                }.map { apiDataModel ->
                    val mappedResult = popularMoviesDataMapper.map(apiDataModel)
                    localDataSource.savePopular(mappedResult)
                    mappedResult
                }.onSuccess { convertedData ->
                    emit(
                        Resource.Success(
                            updatePopularMovies(
                                convertedData,
                                savedFavoriteMovies,
                            ),
                        ),
                    )
                }.recoverCatching { error ->
                    emit(Resource.Error(ErrorType.Generic(error.message)))
                }
            }
        }

    override fun getUpcomingMovies(): Flow<Resource<ImmutableList<UpcomingMoviesEntity>>> =
        flow {
            val localUpcomingMovies = localDataSource.getUpcoming().firstOrNull()
            val savedFavoriteMovies = getSavedFavoriteMovies().firstOrNull()

            if (!localUpcomingMovies.isNullOrEmpty()) {
                emit(
                    Resource.Success(
                        updateUpcomingMovies(
                            localUpcomingMovies,
                            savedFavoriteMovies,
                        ),
                    ),
                )
            } else {
                runCatching {
                    remoteDataSource.fetchUpcoming()
                }.onFailure { failure ->
                    failure.message
                        .takeIf { it.isNullOrEmpty().not() }
                        ?.let { metricsEventsDispatcher.onEvent(it) }
                }.map { apiDataModel ->
                    val mappedResult = upcomingMoviesDataMapper.map(apiDataModel)
                    localDataSource.saveUpcoming(mappedResult)
                    mappedResult
                }.onSuccess { convertedData ->
                    emit(
                        Resource.Success(
                            updateUpcomingMovies(
                                convertedData,
                                savedFavoriteMovies,
                            ),
                        ),
                    )
                }.recoverCatching { error ->
                    emit(Resource.Error(ErrorType.Generic(error.message)))
                }
            }
        }

    override fun getNowPlayingMovies(): Flow<Resource<ImmutableList<NowPlayingMoviesEntity>>> =
        flow {
            val nowPlayingMovies = localDataSource.getNowPlaying().firstOrNull()
            val savedFavoriteMovies = getSavedFavoriteMovies().firstOrNull()

            if (!nowPlayingMovies.isNullOrEmpty()) {
                emit(
                    Resource.Success(
                        updateNowPlayingMovies(
                            nowPlayingMovies,
                            savedFavoriteMovies,
                        ),
                    ),
                )
            } else {
                runCatching {
                    remoteDataSource.fetchNowPlaying()
                }.onFailure { failure ->
                    failure.message
                        .takeIf { it.isNullOrEmpty().not() }
                        ?.let { metricsEventsDispatcher.onEvent(it) }
                }.map { apiDataModel ->
                    val mappedResult = nowPlayingMoviesDataMapper.map(apiDataModel)
                    localDataSource.saveNowPlaying(mappedResult)
                    mappedResult
                }.onSuccess { convertedData ->
                    emit(
                        Resource.Success(
                            updateNowPlayingMovies(
                                convertedData,
                                savedFavoriteMovies,
                            ),
                        ),
                    )
                }.recoverCatching { error ->
                    emit(Resource.Error(ErrorType.Generic(error.message)))
                }
            }
        }

    override fun addFavorite(id: Int): Flow<Resource<AddToFavoriteMoviesData>> =
        flow {
            runCatching {
                val request = AddToFavoritesRequest(mediaId = id, favorite = true)
                remoteDataSource.addOrRemoveFromFavorites(request)
            }.onFailure { failure ->
                failure.message
                    .takeIf { it.isNullOrEmpty().not() }
                    ?.let { metricsEventsDispatcher.onEvent(it) }
            }.map { apiDataModel ->
                val mappedResult = addOrRemoveToFavoritesResponseDataMapper.map(apiDataModel)
                mappedResult
            }.onSuccess { convertedData ->
                emit(Resource.Success(convertedData))
            }.recoverCatching { error ->
                emit(Resource.Error(ErrorType.Generic(error.message)))
            }
        }

    override fun removeFavorite(id: Int): Flow<Resource<AddToFavoriteMoviesData>> =
        flow {
            runCatching {
                val request = AddToFavoritesRequest(mediaId = id, favorite = false)
                localDataSource.removeFavoriteMovie(id)
                remoteDataSource.addOrRemoveFromFavorites(request)
            }.onFailure { failure ->
                failure.message
                    .takeIf { it.isNullOrEmpty().not() }
                    ?.let { metricsEventsDispatcher.onEvent(it) }
            }.map { apiDataModel ->
                val mappedResult = addOrRemoveToFavoritesResponseDataMapper.map(apiDataModel)
                mappedResult
            }.onSuccess { convertedData ->
                emit(Resource.Success(convertedData))
            }.recoverCatching { error ->
                emit(Resource.Error(ErrorType.Generic(error.message)))
            }
        }

    override fun getFavorites(): Flow<Resource<ImmutableList<FavoriteMoviesEntity>>> =
        flow {
            val favorites = localDataSource.getFavoriteMovies().first()

            if (favorites.isNullOrEmpty().not() && favorites.all { isCacheValid(it.lastUpdated) }) {
                emit(Resource.Success(favorites.toImmutableList()))
                return@flow
            }

            runCatching {
                remoteDataSource.fetchFavorites()
            }.onFailure { failure ->
                failure.message
                    .takeIf { it.isNullOrEmpty().not() }
                    ?.let { metricsEventsDispatcher.onEvent(it) }
            }.map { apiDataModel ->
                val mappedResult = favoritesDataMapper.map(apiDataModel)
                localDataSource.saveFavorites(mappedResult)
                mappedResult
            }.onSuccess { convertedData ->
                emit(Resource.Success(convertedData.toImmutableList()))
            }.recoverCatching { error ->
                emit(Resource.Error(ErrorType.Generic(error.message)))
            }
        }

    private fun getSavedFavoriteMovies(): Flow<List<FavoriteMoviesEntity>?> =
        flow {
            val favorites = localDataSource.getFavoriteMovies().first()

            if (!favorites.isNullOrEmpty()) {
                emit(favorites)
                return@flow
            }

            runCatching {
                remoteDataSource.fetchFavorites()
            }.onFailure { failure ->
                failure.message
                    .takeIf { it.isNullOrEmpty().not() }
                    ?.let { metricsEventsDispatcher.onEvent(it) }
            }.map { apiDataModel ->
                val mappedResult = favoritesDataMapper.map(apiDataModel)
                localDataSource.saveFavorites(mappedResult)
                mappedResult
            }.onSuccess { convertedData ->
                emit(convertedData)
            }.recoverCatching { _ ->
                emit(null)
            }
        }

    override suspend fun refreshAll(): SyncResult =
        SyncResult(
            mapOf(
                SyncCategory.NOW_PLAYING to
                    refresh(
                        fetch = { nowPlayingMoviesDataMapper.map(remoteDataSource.fetchNowPlaying()) },
                        save = localDataSource::saveNowPlaying,
                    ),
                SyncCategory.POPULAR to
                    refresh(
                        fetch = { popularMoviesDataMapper.map(remoteDataSource.fetchPopular()) },
                        save = localDataSource::savePopular,
                    ),
                SyncCategory.TOP_RATED to
                    refresh(
                        fetch = { topRatedMoviesDataMapper.map(remoteDataSource.fetchTopRated()) },
                        save = localDataSource::saveTopRated,
                    ),
                SyncCategory.UPCOMING to
                    refresh(
                        fetch = { upcomingMoviesDataMapper.map(remoteDataSource.fetchUpcoming()) },
                        save = localDataSource::saveUpcoming,
                    ),
                SyncCategory.FAVORITES to
                    refresh(
                        fetch = { favoritesDataMapper.map(remoteDataSource.fetchFavorites()) },
                        save = localDataSource::saveFavorites,
                    ),
            ),
        )

    private suspend fun <T> refresh(
        fetch: suspend () -> List<T>,
        save: (List<T>) -> Unit,
    ): SyncOutcome =
        try {
            val movies = fetch()
            if (movies.isNullOrEmpty().not()) {
                save(movies)
                SyncOutcome.Success(movies.size)
            } else {
                SyncOutcome.Failure("No data fetched from remote source")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.message
                .takeIf { it.isNullOrEmpty().not() }
                ?.let { metricsEventsDispatcher.onEvent(it) }
            SyncOutcome.Failure(e.message)
        }
}
