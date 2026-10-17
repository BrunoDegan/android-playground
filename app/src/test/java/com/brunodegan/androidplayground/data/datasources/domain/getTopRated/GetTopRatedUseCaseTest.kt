package com.brunodegan.androidplayground.data.datasources.domain.getTopRated

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.domain.getTopRated.GetTopRatedUseCase
import com.brunodegan.androidplayground.domain.getTopRated.GetTopRatedUseCaseImpl
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.repositories.MoviesRepository
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

class GetTopRatedUseCaseTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private val repository: MoviesRepository = mockk()
    private lateinit var useCase: GetTopRatedUseCase

    @Before
    fun setUp() {
        useCase = GetTopRatedUseCaseImpl(repository = repository)
    }

    @Test
    fun `GIVEN top rated movies WHEN invoke is called THEN emit Resource Success`() =
        runTest {
            // Given
            val expectedData = Resource.Success(MockUtils.mockTopRatedMovies())
            coEvery { repository.getTopRateMovies() } returns
                flow {
                    emit(expectedData)
                }

            // When
            val result = useCase.invoke()

            // Then
            coVerify(exactly = 1) {
                repository.getTopRateMovies()
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
            val exception = Exception("Error fetching now top rated movies")
            val resourceError = getResourceError<ImmutableList<Movie>>(exception)

            coEvery { repository.getTopRateMovies() } returns
                flow {
                    emit(resourceError)
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertTrue {
                result.first() is Resource.Error<ImmutableList<Movie>>
            }
            assertEquals(
                "Error fetching now top rated movies",
                (result.first() as Resource.Error).error.message,
            )
        }

    @Test
    fun `GIVEN no top rated movies WHEN invoke is called THEN emit Resource_Success with empty list`() =
        runTest {
            // GIVEN
            coEvery { repository.getTopRateMovies() } returns
                flow {
                    emit(Resource.Success(persistentListOf()))
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertEquals(Resource.Success(persistentListOf<Movie>()), result.first())
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
