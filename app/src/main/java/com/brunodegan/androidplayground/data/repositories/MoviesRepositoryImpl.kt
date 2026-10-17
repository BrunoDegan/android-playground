package com.brunodegan.androidplayground.data.repositories

import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.utils.isCacheValid
import com.brunodegan.androidplayground.base.utils.toSortableDate
import com.brunodegan.androidplayground.data.datasources.local.LocalDataSource
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesRequest
import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.update
import com.brunodegan.androidplayground.data.datasources.remote.RemoteDataSource
import com.brunodegan.androidplayground.data.mappers.addToFavorites.AddToFavoriteResultMapper
import com.brunodegan.androidplayground.data.mappers.favorites.AddOrRemoveToFavoritesResponseDataMapper
import com.brunodegan.androidplayground.data.mappers.favorites.FavoritesDataMapper
import com.brunodegan.androidplayground.data.mappers.movies.MoviesEntityMapper
import com.brunodegan.androidplayground.data.mappers.nowPlaying.NowPlayingDataMapper
import com.brunodegan.androidplayground.data.mappers.popular.PopularDataMapper
import com.brunodegan.androidplayground.data.mappers.topRated.TopRatedDataMapper
import com.brunodegan.androidplayground.data.mappers.upComing.UpcomingDataMapper
import com.brunodegan.androidplayground.data.metrics.Metrics
import com.brunodegan.androidplayground.domain.model.FavoriteResult
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.model.SyncCategory
import com.brunodegan.androidplayground.domain.model.SyncOutcome
import com.brunodegan.androidplayground.domain.model.SyncResult
import com.brunodegan.androidplayground.domain.repositories.MoviesRepository
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
    private val addToFavoriteResultMapper: AddToFavoriteResultMapper,
    private val moviesEntityMapper: MoviesEntityMapper,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val metricsEventsDispatcher: Metrics,
) : MoviesRepository {
    private fun updateTopRatedMovies(
        movies: List<TopRatedMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<Movie> =
        (favorites?.let { movies.update(it) } ?: movies)
            .map { moviesEntityMapper.map(it) }
            .sortedByDescending { it.releaseDate.toSortableDate() }
            .toImmutableList()

    private fun updatePopularMovies(
        movies: List<PopularMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<Movie> =
        (favorites?.let { movies.update(it) } ?: movies)
            .map { moviesEntityMapper.map(it) }
            .sortedByDescending { it.releaseDate.toSortableDate() }
            .toImmutableList()

    private fun updateUpcomingMovies(
        movies: List<UpcomingMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<Movie> =
        (favorites?.let { movies.update(it) } ?: movies)
            .map { moviesEntityMapper.map(it) }
            .sortedByDescending { it.releaseDate.toSortableDate() }
            .toImmutableList()

    private fun updateNowPlayingMovies(
        movies: List<NowPlayingMoviesEntity>,
        favorites: List<FavoriteMoviesEntity>?,
    ): ImmutableList<Movie> =
        (favorites?.let { movies.update(it) } ?: movies)
            .map { moviesEntityMapper.map(it) }
            .sortedByDescending { it.releaseDate.toSortableDate() }
            .toImmutableList()

    override fun getTopRateMovies(): Flow<Resource<ImmutableList<Movie>>> =
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

    override fun getPopularMovies(): Flow<Resource<ImmutableList<Movie>>> =
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

    override fun getUpcomingMovies(): Flow<Resource<ImmutableList<Movie>>> =
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

    override fun getNowPlayingMovies(): Flow<Resource<ImmutableList<Movie>>> =
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

    override fun addFavorite(id: Int): Flow<Resource<FavoriteResult>> =
        flow {
            runCatching {
                val request = AddToFavoritesRequest(mediaId = id, favorite = true)
                remoteDataSource.addOrRemoveFromFavorites(request)
            }.onFailure { failure ->
                failure.message
                    .takeIf { it.isNullOrEmpty().not() }
                    ?.let { metricsEventsDispatcher.onEvent(it) }
            }.map { apiDataModel ->
                addToFavoriteResultMapper.map(
                    addOrRemoveToFavoritesResponseDataMapper.map(
                        apiDataModel,
                    ),
                )
            }.onSuccess { convertedData ->
                emit(Resource.Success(convertedData))
            }.recoverCatching { error ->
                emit(Resource.Error(ErrorType.Generic(error.message)))
            }
        }

    override fun removeFavorite(id: Int): Flow<Resource<FavoriteResult>> =
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
                addToFavoriteResultMapper.map(
                    addOrRemoveToFavoritesResponseDataMapper.map(
                        apiDataModel,
                    ),
                )
            }.onSuccess { convertedData ->
                emit(Resource.Success(convertedData))
            }.recoverCatching { error ->
                emit(Resource.Error(ErrorType.Generic(error.message)))
            }
        }

    override fun getFavorites(): Flow<Resource<ImmutableList<Movie>>> =
        flow {
            val favorites = localDataSource.getFavoriteMovies().first()

            if (favorites.isNullOrEmpty().not() && favorites.all { isCacheValid(it.lastUpdated) }) {
                emit(
                    Resource.Success(
                        favorites
                            .map { moviesEntityMapper.map(it) }
                            .sortedByDescending { it.releaseDate.toSortableDate() }
                            .toImmutableList(),
                    ),
                )
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
                emit(
                    Resource.Success(
                        convertedData
                            .map { moviesEntityMapper.map(it) }
                            .sortedByDescending { it.releaseDate.toSortableDate() }
                            .toImmutableList(),
                    ),
                )
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
                        allowEmpty = true,
                    ),
            ),
        )

    private suspend fun <T> refresh(
        fetch: suspend () -> List<T>,
        save: (List<T>) -> Unit,
        allowEmpty: Boolean = false,
    ): SyncOutcome =
        try {
            val movies = fetch()
            if (allowEmpty || movies.isNotEmpty()) {
                save(movies)
                SyncOutcome.Success(movies.size)
            } else {
                SyncOutcome.Failure("No data fetched from remote source")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Class name only: exception messages can embed the request URL (account id in path)
            metricsEventsDispatcher.onEvent("sync_failed: ${e::class.simpleName}")
            SyncOutcome.Failure(e.message)
        }
}
