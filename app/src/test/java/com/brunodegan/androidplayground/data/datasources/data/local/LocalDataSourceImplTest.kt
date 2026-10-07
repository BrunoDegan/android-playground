package com.brunodegan.androidplayground.data.datasources.data.local

import com.brunodegan.androidplayground.data.datasources.local.LocalDataSource
import com.brunodegan.androidplayground.data.datasources.local.LocalDataSourceImpl
import com.brunodegan.androidplayground.data.datasources.local.daos.FavoritesDao
import com.brunodegan.androidplayground.data.datasources.local.daos.NowPlayingDao
import com.brunodegan.androidplayground.data.datasources.local.daos.PopularDao
import com.brunodegan.androidplayground.data.datasources.local.daos.TopRatedDao
import com.brunodegan.androidplayground.data.datasources.local.daos.UpComingDao
import com.brunodegan.androidplayground.testfixtures.MockUtils
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LocalDataSourceImplTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private lateinit var localDataSource: LocalDataSource
    private val favoritesDao: FavoritesDao = mockk()
    private val nowPlayingDao: NowPlayingDao = mockk()
    private val topRatedDao: TopRatedDao = mockk()
    private val upComingDao: UpComingDao = mockk()
    private val popularDao: PopularDao = mockk()

    @Before
    fun setup() {
        localDataSource =
            LocalDataSourceImpl(
                favoriteDao = favoritesDao,
                nowPlayingDao = nowPlayingDao,
                topRatedDao = topRatedDao,
                upComingDao = upComingDao,
                popularDao = popularDao,
            )
    }

    @Test
    fun `GIVEN favorite movies entity mocks WHEN local storage saves favorite movies THEN assert favoriteDao inserts is called`() =
        runTest {
            val mockFavoritesMovies = MockUtils.mockFavoriteMoviesEntity()
            coJustRun { favoritesDao.insertFavorite(mockFavoritesMovies) }

            localDataSource.saveFavorites(mockFavoritesMovies)

            verify(exactly = 1) {
                favoritesDao.insertFavorite(mockFavoritesMovies)
            }
        }

    @Test
    fun `GIVEN favorite movies entity mocks WHEN local storage gets favorite movies THEN asserts mocks equality and calling once`() =
        runTest {
            val mockFavorites = MockUtils.mockFavoriteMoviesEntity()
            coEvery { favoritesDao.getFavoriteMovies() } returns flowOf(mockFavorites)

            val result = localDataSource.getFavoriteMovies()

            result.collect { favorites ->
                assertEquals(mockFavorites, favorites)
            }
            coVerify(exactly = 1) {
                favoritesDao.getFavoriteMovies()
            }
        }

    @Test
    fun `GIVEN upcoming movies entity mocks WHEN local storage saves upcoming movies THEN asserts mocks equality and calling once`() =
        runTest {
            val mockUpComingMovies = MockUtils.mockUpcomingMoviesEntity()

            coEvery { upComingDao.getAllUpcoming() } returns flowOf(mockUpComingMovies)
            val result = localDataSource.getUpcoming()

            result.collect { upcomingMovies ->
                assertEquals(mockUpComingMovies, upcomingMovies)
            }
            coVerify(exactly = 1) {
                upComingDao.getAllUpcoming()
            }
        }

    @Test
    fun `GIVEN now playing movies entity mocks WHEN local storage saves now playing movies THEN asserts mocks equality and calling once`() =
        runTest {
            val mockNowPlayingMovies = MockUtils.mockNowPlayingMoviesEntity()

            coEvery { nowPlayingDao.getAllNowPlaying() } returns flowOf(mockNowPlayingMovies)
            val result = localDataSource.getNowPlaying()

            result.collect { nowPlaying ->
                assertEquals(mockNowPlayingMovies, nowPlaying)
            }
            coVerify(exactly = 1) {
                nowPlayingDao.getAllNowPlaying()
            }
        }

    @Test
    fun `GIVEN now popular movies entity mocks WHEN local storage saves popular movies THEN  asserts mocks equality and calling once`() =
        runTest {
            val mockPopularMovies = MockUtils.mockPopularMoviesEntity()

            coEvery { popularDao.getAllPopular() } returns flowOf(mockPopularMovies)
            val result = localDataSource.getPopular()

            result.collect { popular ->
                assertEquals(mockPopularMovies, popular)
            }
            coVerify(exactly = 1) {
                popularDao.getAllPopular()
            }
        }

    @Test
    fun `GIVEN top rated movies entity mocks WHEN local storage saves popular movies THEN  asserts mocks equality and calling once`() =
        runTest {
            val mockTopRatedMovies = MockUtils.mockTopRatedMoviesEntity()

            coEvery { topRatedDao.getAllTopRated() } returns flowOf(mockTopRatedMovies)
            val result = localDataSource.getTopRated()

            result.collect { topRatedMovies ->
                assertEquals(mockTopRatedMovies, topRatedMovies)
            }
            coVerify(exactly = 1) {
                topRatedDao.getAllTopRated()
            }
        }

    @Test
    fun `GIVEN now playing movies entity mocks WHEN local storage saves now playing movie THEN inserts into dao once`() =
        runTest {
            val mockNowPlayingMovies = MockUtils.mockNowPlayingMoviesEntity()
            coJustRun { nowPlayingDao.insertNowPlayingMovies(mockNowPlayingMovies) }

            localDataSource.saveNowPlaying(mockNowPlayingMovies)

            coVerify(exactly = 1) {
                nowPlayingDao.insertNowPlayingMovies(mockNowPlayingMovies)
            }
        }

    @Test
    fun `GIVEN popular movies entity mocks WHEN local storage saves popular movies THEN inserts into dao once`() =
        runTest {
            val mockPopularMovies = MockUtils.mockPopularMoviesEntity()
            coJustRun { popularDao.insertPopularMovies(mockPopularMovies) }

            localDataSource.savePopular(mockPopularMovies)

            coVerify(exactly = 1) {
                popularDao.insertPopularMovies(mockPopularMovies)
            }
        }

    @Test
    fun `GIVEN top rated movies entity mocks WHEN local storage saves top rated movies THEN inserts into dao once`() =
        runTest {
            val mockTopRatedMovies = MockUtils.mockTopRatedMoviesEntity()
            coJustRun { topRatedDao.insertTopRatedMovies(mockTopRatedMovies) }

            localDataSource.saveTopRated(mockTopRatedMovies)

            coVerify(exactly = 1) {
                topRatedDao.insertTopRatedMovies(mockTopRatedMovies)
            }
        }

    @Test
    fun `GIVEN upcoming movies entity mocks WHEN local storage saves upcoming movies THEN inserts into dao once`() =
        runTest {
            val mockUpcomingMovies = MockUtils.mockUpcomingMoviesEntity()
            coJustRun { upComingDao.insertUpcomingMovies(mockUpcomingMovies) }

            localDataSource.saveUpcoming(mockUpcomingMovies)

            coVerify(exactly = 1) {
                upComingDao.insertUpcomingMovies(mockUpcomingMovies)
            }
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
