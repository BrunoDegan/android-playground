package com.brunodegan.androidplayground.data.datasources.data.repositories

import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.LocalDataSource
import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.MovieEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
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
import com.brunodegan.androidplayground.data.repositories.MoviesRepositoryImpl
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockAddToFavoriteMoviesData
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockAddToFavoritesApiResponse
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockAddToFavoritesRequest
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockFavoriteMoviesEntity
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMoviesApiDataResponse
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockNowPlayingMoviesEntity
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockPopularMoviesEntity
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockTopRatedMoviesEntity
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesRepositoryTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private val localDataSource: LocalDataSource = mockk()
    private val remoteDataSource: RemoteDataSource = mockk()
    private val topRatedDataMapper: TopRatedDataMapper = mockk()
    private val upcomingMoviesDataMapper: UpcomingDataMapper = mockk()
    private val popularMoviesDataMapper: PopularDataMapper = mockk()
    private val nowPlayingMoviesDataMapper: NowPlayingDataMapper = mockk()
    private val addOrRemoveToFavoritesResponseDataMapper: AddOrRemoveToFavoritesResponseDataMapper =
        mockk()
    private val favoritesDataMapper: FavoritesDataMapper = mockk()
    private val metricsEventDispatcher: Metrics = mockk()

    private val entityMapper = MoviesEntityMapper()
    private val resultMapper = AddToFavoriteResultMapper()

    // Repository returns newest first; fixture dates are ISO so plain string order is chronological
    private fun List<MovieEntity>.toExpected() = map { entityMapper.map(it) }.sortedByDescending { it.releaseDate }.toImmutableList()

    private lateinit var repository: MoviesRepositoryImpl

    @Before
    fun setUp() {
        repository =
            MoviesRepositoryImpl(
                addOrRemoveToFavoritesResponseDataMapper = addOrRemoveToFavoritesResponseDataMapper,
                favoritesDataMapper = favoritesDataMapper,
                nowPlayingMoviesDataMapper = nowPlayingMoviesDataMapper,
                popularMoviesDataMapper = popularMoviesDataMapper,
                topRatedMoviesDataMapper = topRatedDataMapper,
                upcomingMoviesDataMapper = upcomingMoviesDataMapper,
                addToFavoriteResultMapper = resultMapper,
                moviesEntityMapper = entityMapper,
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                metricsEventsDispatcher = metricsEventDispatcher,
            )
    }

    @Test
    fun `GIVEN local data WHEN getTopRateMovies is called THEN emit Resource_Success`() =
        runTest {
            // Given
            val topRatedMovies = mockTopRatedMoviesEntity()
            val favoritesMovies = mockFavoriteMoviesEntity()
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getTopRated() } returns flow { emit(topRatedMovies) }

            // When
            val result = repository.getTopRateMovies()

            // Then
            assertEquals(Resource.Success(topRatedMovies.toExpected()), result.first())
        }

    @Test
    fun `GIVEN local movies with BR dates and a blank one WHEN getTopRateMovies is called THEN newest first and blank last`() =
        runTest {
            val base = mockTopRatedMoviesEntity().first()
            val dates = listOf("05/01/2024", "20/12/2023", "", "01/02/2024")
            val local = dates.mapIndexed { index, date -> base.copy(id = index + 1, releaseDate = date) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(mockFavoriteMoviesEntity()) }
            coEvery { localDataSource.getTopRated() } returns flow { emit(local) }

            val result = repository.getTopRateMovies().first() as Resource.Success

            assertEquals(listOf("01/02/2024", "05/01/2024", "20/12/2023", ""), result.data.map { it.releaseDate })
        }

    @Test
    fun `GIVEN empty local data and fetchs remote data successfully WHEN getTopRateMovies is called THEN emit Resource_Success`() =
        runTest {
            // GIVEN
            val topRatedMovies = mockTopRatedMoviesEntity()
            val apiData = mockMoviesApiDataResponse()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.saveTopRated(topRatedMovies) }
            coJustRun { localDataSource.saveFavorites(favoritesMovies) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getTopRated() } returns flow { emit(emptyList()) }
            coEvery { remoteDataSource.fetchTopRated() } returns apiData
            coEvery { topRatedDataMapper.map(any()) } returns topRatedMovies

            // WHEN
            val result = repository.getTopRateMovies().toList()
//            advanceUntilIdle()

            // THEN
            assertEquals(Resource.Success(topRatedMovies.toExpected()), result.first())
        }

    @Test
    fun `GIVEN NULL local data and fetchs remote data with error WHEN getTopRateMovies is called THEN emit Resource_Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Network error"
            val genericError = ErrorType.Generic(errorMessage)
            val expectedError =
                Resource.Error<TopRatedMoviesEntity>(genericError)
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { metricsEventDispatcher.onEvent(any()) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getTopRated() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchTopRated() } throws Exception(errorMessage)

            // WHEN
            val result = repository.getTopRateMovies().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedError, result.first())
            coVerify { metricsEventDispatcher.onEvent(errorMessage) }
        }

    @Test
    fun `GIVEN TopRated mock api response data WHEN local data source answers with null and remote data source fetchs data successfully THEN asserts Success Data`() =
        runTest {
            // GIVEN
            val topRatedMoviesApiResponse = mockMoviesApiDataResponse()
            val expectedViewData = mockTopRatedMoviesEntity()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.saveTopRated(expectedViewData) }
            coJustRun { localDataSource.saveFavorites(favoritesMovies) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getTopRated() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchTopRated() } returns topRatedMoviesApiResponse
            every { topRatedDataMapper.map(any()) } returns expectedViewData

            // WHEN
            val result = repository.getTopRateMovies().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedViewData.toExpected(), (result.first() as Resource.Success).data)
        }

    @Test
    fun `GIVEN favorites local data WHEN getFavorites is called THEN emit Resource_Success`() =
        runTest {
            // Given
            val favoritesMovies = mockFavoriteMoviesEntity()
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }

            // When
            val result = repository.getFavorites()

            // Then
            assertEquals(Resource.Success(favoritesMovies.toExpected()), result.first())
        }

    @Test
    fun `GIVEN empty favorites local data and fetchs remote data successfully WHEN getFavorites is called THEN emit Resource_Success`() =
        runTest {
            // GIVEN
            val favoriteMoviesApiResponse = mockFavoriteMoviesEntity()
            val apiData = mockMoviesApiDataResponse()

            coJustRun { localDataSource.saveFavorites(favoriteMoviesApiResponse) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(emptyList()) }
            coEvery { remoteDataSource.fetchFavorites() } returns apiData
            coEvery { favoritesDataMapper.map(any()) } returns favoriteMoviesApiResponse

            // WHEN
            val result = repository.getFavorites().toList()

            // THEN
            assertEquals(Resource.Success(favoriteMoviesApiResponse.toExpected()), result.first())
        }

    @Test
    fun `GIVEN NULL favorites local data and fetchs remote data with error WHEN getFavorites is called THEN emit Resource_Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Network error"
            val genericError = ErrorType.Generic(errorMessage)
            val expectedError =
                Resource.Error<FavoriteMoviesEntity>(genericError)

            coJustRun { metricsEventDispatcher.onEvent(any()) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchFavorites() } throws Exception(errorMessage)

            // WHEN
            val result = repository.getFavorites().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedError, result.first())
            coVerify { metricsEventDispatcher.onEvent(errorMessage) }
        }

    @Test
    fun `GIVEN favorite movies mock api response data WHEN local data source answers with null and remote data source fetchs data successfully THEN asserts Success Data`() =
        runTest {
            // GIVEN
            val favoriteMoviesApiResponse = mockMoviesApiDataResponse()
            val expectedViewData = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.saveFavorites(expectedViewData) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchFavorites() } returns favoriteMoviesApiResponse
            every { favoritesDataMapper.map(any()) } returns expectedViewData

            // WHEN
            val result = repository.getFavorites().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedViewData.toExpected(), (result.first() as Resource.Success).data)
        }

    @Test
    fun `GIVEN popular local data WHEN getPopularMovies is called THEN emit Resource_Success`() =
        runTest {
            // Given
            val popularMovies = mockPopularMoviesEntity()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getPopular() } returns flow { emit(popularMovies) }

            // When
            val result = repository.getPopularMovies()

            // Then
            assertEquals(Resource.Success(popularMovies.toExpected()), result.first())
        }

    @Test
    fun `GIVEN empty popular local data and fetchs remote data successfully WHEN getPopularMovies is called THEN emit Resource_Success`() =
        runTest {
            // GIVEN
            val popularMovies = mockPopularMoviesEntity()
            val apiData = mockMoviesApiDataResponse()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.savePopular(popularMovies) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getPopular() } returns flow { emit(emptyList()) }
            coEvery { remoteDataSource.fetchPopular() } returns apiData
            coEvery { popularMoviesDataMapper.map(any()) } returns popularMovies

            // WHEN
            val result = repository.getPopularMovies().toList()

            // THEN
            assertEquals(Resource.Success(popularMovies.toExpected()), result.first())
        }

    @Test
    fun `GIVEN NULL popular local data and fetch remote data with error WHEN getPopularMovies is called THEN emit Resource_Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Network error"
            val genericError = ErrorType.Generic(errorMessage)
            val expectedError =
                Resource.Error<PopularMoviesEntity>(genericError)
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { metricsEventDispatcher.onEvent(any()) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getPopular() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchPopular() } throws Exception(errorMessage)

            // WHEN
            val result = repository.getPopularMovies().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedError, result.first())
            coVerify { metricsEventDispatcher.onEvent(errorMessage) }
        }

    @Test
    fun `GIVEN popular movies mock api response data WHEN local data source answers with null and remote data source fetchs data successfully THEN asserts Success Data`() =
        runTest {
            // GIVEN
            val popularMoviesApiResponse = mockMoviesApiDataResponse()
            val expectedViewData = mockPopularMoviesEntity()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.savePopular(expectedViewData) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getPopular() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchPopular() } returns popularMoviesApiResponse
            every { popularMoviesDataMapper.map(any()) } returns expectedViewData

            // WHEN
            val result = repository.getPopularMovies().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedViewData.toExpected(), (result.first() as Resource.Success).data)
        }

    @Test
    fun `GIVEN empty now playing local data and fetchs remote data successfully WHEN getNowPlayingMovies is called THEN emit Resource_Success`() =
        runTest {
            // GIVEN
            val nowPlaying = mockNowPlayingMoviesEntity()
            val apiData = mockMoviesApiDataResponse()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.saveNowPlaying(nowPlaying) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getNowPlaying() } returns flow { emit(emptyList()) }
            coEvery { remoteDataSource.fetchNowPlaying() } returns apiData
            coEvery { nowPlayingMoviesDataMapper.map(any()) } returns nowPlaying

            // WHEN
            val result = repository.getNowPlayingMovies().toList()

            // THEN
            assertEquals(Resource.Success(nowPlaying.toExpected()), result.first())
        }

    @Test
    fun `GIVEN NULL now playing local data and fetchs remote data with error WHEN getNowPlayingMovies is called THEN emit Resource_Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Network error"
            val genericError = ErrorType.Generic(errorMessage)
            val expectedError =
                Resource.Error<PopularMoviesEntity>(genericError)
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { metricsEventDispatcher.onEvent(any()) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getNowPlaying() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchNowPlaying() } throws Exception(errorMessage)

            // WHEN
            val result = repository.getNowPlayingMovies().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedError, result.first())
            coVerify { metricsEventDispatcher.onEvent(errorMessage) }
        }

    @Test
    fun `GIVEN now playing movies mock api response data WHEN local data source answers with null and remote data source fetchs data successfully THEN asserts Success Data`() =
        runTest {
            // GIVEN
            val nowPlayingMoviesApiResponse = mockMoviesApiDataResponse()
            val expectedViewData = mockNowPlayingMoviesEntity()
            val favoritesMovies = mockFavoriteMoviesEntity()

            coJustRun { localDataSource.saveNowPlaying(expectedViewData) }
            coJustRun { localDataSource.saveFavorites(favoritesMovies) }
            coEvery { localDataSource.getFavoriteMovies() } returns flow { emit(favoritesMovies) }
            coEvery { localDataSource.getNowPlaying() } returns flow { emit(null) }
            coEvery { remoteDataSource.fetchNowPlaying() } returns nowPlayingMoviesApiResponse
            every { nowPlayingMoviesDataMapper.map(any()) } returns expectedViewData

            // WHEN
            val result = repository.getNowPlayingMovies().toList()

            // THEN
            assertEquals(1, result.size)
            assertEquals(expectedViewData.toExpected(), (result.first() as Resource.Success).data)
        }

    @Test
    fun `GIVEN favorite movie data WHEN addFavorite THEN `() =
        runTest {
            val expectedViewData = mockAddToFavoriteMoviesData()
            val requestData = mockAddToFavoritesRequest()
            val mockAddToFavoritesApiResponse = mockAddToFavoritesApiResponse()

            coEvery { remoteDataSource.addOrRemoveFromFavorites(requestData) } returns mockAddToFavoritesApiResponse
            coEvery { addOrRemoveToFavoritesResponseDataMapper.map(mockAddToFavoritesApiResponse) } returns expectedViewData

            val result = repository.addFavorite(id = 1)

            assertEquals(resultMapper.map(expectedViewData), (result.first() as Resource.Success).data)
        }

    @Test
    fun `GIVEN network error when fetching favorite movies WHEN addOrRemoveFromFavorites on remote data source THEN asserts Resource_Error result`() =
        runTest {
            val expectedViewData = mockAddToFavoriteMoviesData()
            val mockAddToFavoritesApiResponse = mockAddToFavoritesApiResponse()
            val requestData = mockAddToFavoritesRequest()
            val errorMessage = "Network error"
            val genericError = ErrorType.Generic(errorMessage)
            val expectedError = Resource.Error<FavoriteMoviesEntity>(genericError)

            coJustRun { metricsEventDispatcher.onEvent(any()) }
            coEvery { addOrRemoveToFavoritesResponseDataMapper.map(mockAddToFavoritesApiResponse) } returns expectedViewData
            coEvery { remoteDataSource.addOrRemoveFromFavorites(requestData) } throws
                Exception(
                    errorMessage,
                )

            val result = repository.addFavorite(id = 1)

            assertEquals(expectedError, result.first())
            coVerify { metricsEventDispatcher.onEvent(errorMessage) }
        }

    @After
    fun tearDown() = unmockkAll()
}
