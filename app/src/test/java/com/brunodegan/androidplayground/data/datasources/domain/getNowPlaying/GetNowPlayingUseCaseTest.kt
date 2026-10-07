package com.brunodegan.androidplayground.data.datasources.domain.getNowPlaying

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.repositories.MoviesRepository
import com.brunodegan.androidplayground.domain.getNowPlaying.GetNowPlayingUseCase
import com.brunodegan.androidplayground.domain.getNowPlaying.GetNowPlayingUseCaseImpl
import com.brunodegan.androidplayground.testfixtures.MockUtils
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class GetNowPlayingUseCaseTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private val repository: MoviesRepository = mockk()
    private lateinit var useCase: GetNowPlayingUseCase

    @Before
    fun setUp() {
        useCase = GetNowPlayingUseCaseImpl(repository = repository)
    }

    @Test
    fun `GIVEN now playing movies WHEN invoke is called THEN emit Resource Success`() =
        runTest {
            // Given
            val expectedData = Resource.Success(MockUtils.mockNowPlayingMoviesEntity())
            coEvery { repository.getNowPlayingMovies() } returns
                flow {
                    emit(expectedData)
                }

            // When
            val result = useCase.invoke()

            // Then
            coVerify(exactly = 1) {
                repository.getNowPlayingMovies()
            }
            assertEquals(
                expectedData,
                result.first(),
            )
        }

    @Test
    fun `GIVEN an exception WHEN invoke is called THEN emit ResourceError`() =
        runTest {
            // GIVEN
            val exception = Exception("Error fetching now playing movies")
            val resourceError = getResourceError<ImmutableList<NowPlayingMoviesEntity>>(exception)

            coEvery { repository.getNowPlayingMovies() } returns
                flow {
                    emit(resourceError)
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertTrue {
                result.first() is Resource.Error<ImmutableList<NowPlayingMoviesEntity>>
            }
            assertEquals(
                "Error fetching now playing movies",
                (result.first() as Resource.Error).error.message,
            )
        }

    @Test
    fun `GIVEN no now playing movies WHEN invoke is called THEN emit Resource_Success with empty list`() =
        runTest {
            // GIVEN
            coEvery { repository.getNowPlayingMovies() } returns
                flow {
                    emit(Resource.Success(persistentListOf()))
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertEquals(Resource.Success(persistentListOf<NowPlayingMoviesEntity>()), result.first())
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
