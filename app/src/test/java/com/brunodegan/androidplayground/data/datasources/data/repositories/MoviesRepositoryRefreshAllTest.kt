package com.brunodegan.androidplayground.data.datasources.data.repositories

import com.brunodegan.androidplayground.data.datasources.local.LocalDataSource
import com.brunodegan.androidplayground.data.datasources.remote.RemoteDataSource
import com.brunodegan.androidplayground.data.mappers.AddOrRemoveToFavoritesResponseDataMapper
import com.brunodegan.androidplayground.data.mappers.FavoritesDataMapper
import com.brunodegan.androidplayground.data.mappers.NowPlayingDataMapper
import com.brunodegan.androidplayground.data.mappers.PopularDataMapper
import com.brunodegan.androidplayground.data.mappers.TopRatedDataMapper
import com.brunodegan.androidplayground.data.mappers.UpcomingDataMapper
import com.brunodegan.androidplayground.data.metrics.Metrics
import com.brunodegan.androidplayground.data.repositories.MoviesRepositoryImpl
import com.brunodegan.androidplayground.data.sync.SyncCategory
import com.brunodegan.androidplayground.data.sync.SyncOutcome
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMoviesApiDataResponse
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockNowPlayingMoviesEntity
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockPopularMoviesEntity
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockTopRatedMoviesEntity
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockUpcomingMoviesEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MoviesRepositoryRefreshAllTest {
    private val localDataSource: LocalDataSource = mockk()
    private val remoteDataSource: RemoteDataSource = mockk()
    private val topRatedMapper: TopRatedDataMapper = mockk()
    private val upcomingMapper: UpcomingDataMapper = mockk()
    private val popularMapper: PopularDataMapper = mockk()
    private val nowPlayingMapper: NowPlayingDataMapper = mockk()
    private val metrics: Metrics = mockk()
    private val apiData = mockMoviesApiDataResponse()

    private lateinit var repository: MoviesRepositoryImpl

    @Before
    fun setUp() {
        repository =
            MoviesRepositoryImpl(
                addOrRemoveToFavoritesResponseDataMapper = mockk<AddOrRemoveToFavoritesResponseDataMapper>(),
                favoritesDataMapper = mockk<FavoritesDataMapper>(),
                nowPlayingMoviesDataMapper = nowPlayingMapper,
                popularMoviesDataMapper = popularMapper,
                topRatedMoviesDataMapper = topRatedMapper,
                upcomingMoviesDataMapper = upcomingMapper,
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                metricsEventsDispatcher = metrics,
            )
        justRun { metrics.onEvent(any()) }
    }

    private fun stubAllRemoteSuccess() {
        coEvery { remoteDataSource.fetchNowPlaying() } returns apiData
        coEvery { remoteDataSource.fetchPopular() } returns apiData
        coEvery { remoteDataSource.fetchTopRated() } returns apiData
        coEvery { remoteDataSource.fetchUpcoming() } returns apiData
        every { nowPlayingMapper.map(any()) } returns mockNowPlayingMoviesEntity()
        every { popularMapper.map(any()) } returns mockPopularMoviesEntity()
        every { topRatedMapper.map(any()) } returns mockTopRatedMoviesEntity()
        every { upcomingMapper.map(any()) } returns mockUpcomingMoviesEntity()
        justRun { localDataSource.saveNowPlaying(any()) }
        justRun { localDataSource.savePopular(any()) }
        justRun { localDataSource.saveTopRated(any()) }
        justRun { localDataSource.saveUpcoming(any()) }
    }

    @Test
    fun `GIVEN all remote succeed WHEN refreshAll THEN all four lists saved and favorites untouched`() =
        runTest {
            stubAllRemoteSuccess()

            val result = repository.refreshAll()

            assertEquals(SyncCategory.entries.toSet(), result.outcomes.keys)
            assertTrue(result.outcomes.values.all { it is SyncOutcome.Success })
            assertFalse(result.allFailed)
            verify(exactly = 1) { localDataSource.saveNowPlaying(any()) }
            verify(exactly = 1) { localDataSource.savePopular(any()) }
            verify(exactly = 1) { localDataSource.saveTopRated(any()) }
            verify(exactly = 1) { localDataSource.saveUpcoming(any()) }
            coVerify(exactly = 0) { remoteDataSource.fetchFavorites() }
            verify(exactly = 0) { localDataSource.saveFavorites(any()) }
            coVerify(exactly = 0) { localDataSource.removeFavoriteMovie(any()) }
        }

    @Test
    fun `GIVEN one remote throws WHEN refreshAll THEN other lists saved and that one fails`() =
        runTest {
            stubAllRemoteSuccess()
            coEvery { remoteDataSource.fetchPopular() } throws IllegalStateException("boom")

            val result = repository.refreshAll()

            assertEquals(SyncOutcome.Failure("boom"), result.outcomes[SyncCategory.POPULAR])
            assertTrue(result.outcomes[SyncCategory.NOW_PLAYING] is SyncOutcome.Success)
            assertFalse(result.allFailed)
            verify(exactly = 0) { localDataSource.savePopular(any()) }
            verify(exactly = 1) { localDataSource.saveNowPlaying(any()) }
            verify(exactly = 1) { localDataSource.saveTopRated(any()) }
            verify(exactly = 1) { localDataSource.saveUpcoming(any()) }
        }

    @Test
    fun `GIVEN all remote throw WHEN refreshAll THEN allFailed and nothing saved`() =
        runTest {
            coEvery { remoteDataSource.fetchNowPlaying() } throws RuntimeException("x")
            coEvery { remoteDataSource.fetchPopular() } throws RuntimeException("x")
            coEvery { remoteDataSource.fetchTopRated() } throws RuntimeException("x")
            coEvery { remoteDataSource.fetchUpcoming() } throws RuntimeException("x")

            val result = repository.refreshAll()

            assertTrue(result.allFailed)
            verify(exactly = 0) { localDataSource.saveNowPlaying(any()) }
            verify(exactly = 0) { localDataSource.savePopular(any()) }
            verify(exactly = 0) { localDataSource.saveTopRated(any()) }
            verify(exactly = 0) { localDataSource.saveUpcoming(any()) }
        }

    @Test
    fun `GIVEN remote returns empty list WHEN refreshAll THEN category fails and local data not overwritten`() =
        runTest {
            stubAllRemoteSuccess()
            every { topRatedMapper.map(any()) } returns emptyList()

            val result = repository.refreshAll()

            assertTrue(result.outcomes[SyncCategory.TOP_RATED] is SyncOutcome.Failure)
            verify(exactly = 0) { localDataSource.saveTopRated(any()) }
        }
}
