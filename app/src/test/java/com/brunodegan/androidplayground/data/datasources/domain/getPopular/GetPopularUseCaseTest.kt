package com.brunodegan.androidplayground.data.datasources.domain.getPopular

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.repositories.MoviesRepository
import com.brunodegan.androidplayground.domain.getPopular.GetPopularUseCase
import com.brunodegan.androidplayground.domain.getPopular.GetPopularUseCaseImpl
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

class GetPopularUseCaseTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private val repository: MoviesRepository = mockk()
    private lateinit var useCase: GetPopularUseCase

    @Before
    fun setUp() {
        useCase = GetPopularUseCaseImpl(repository = repository)
    }

    @Test
    fun `GIVEN popular movies WHEN invoke is called THEN emit Resource_Success`() =
        runTest {
            // Given
            val expectedData = Resource.Success(MockUtils.mockPopularMoviesEntity())
            coEvery { repository.getPopularMovies() } returns
                flow {
                    emit(expectedData)
                }

            // When
            val result = useCase.invoke()

            // Then
            coVerify(exactly = 1) {
                repository.getPopularMovies()
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
            val exception = Exception("Error fetching popular movies")
            val resourceError = getResourceError<ImmutableList<PopularMoviesEntity>>(exception)

            coEvery { repository.getPopularMovies() } returns
                flow {
                    emit(resourceError)
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertTrue {
                result.first() is Resource.Error<ImmutableList<PopularMoviesEntity>>
            }
            assertEquals(
                "Error fetching popular movies",
                (result.first() as Resource.Error).error.message,
            )
        }

    @Test
    fun `GIVEN no popular movies WHEN invoke is called THEN emit Resource_Success with empty list`() =
        runTest {
            // GIVEN
            coEvery { repository.getPopularMovies() } returns
                flow {
                    emit(Resource.Success(persistentListOf()))
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertEquals(Resource.Success(persistentListOf<PopularMoviesEntity>()), result.first())
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
