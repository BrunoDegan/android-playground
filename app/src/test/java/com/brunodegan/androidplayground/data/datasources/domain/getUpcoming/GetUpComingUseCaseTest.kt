package com.brunodegan.androidplayground.data.datasources.domain.getUpcoming

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import com.brunodegan.androidplayground.data.repositories.MoviesRepository
import com.brunodegan.androidplayground.domain.getUpComing.GetUpComingUseCase
import com.brunodegan.androidplayground.domain.getUpComing.GetUpComingUseCaseImpl
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

class GetUpComingUseCaseTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private val repository: MoviesRepository = mockk()
    private lateinit var useCase: GetUpComingUseCase

    @Before
    fun setUp() {
        useCase = GetUpComingUseCaseImpl(repository = repository)
    }

    @Test
    fun `GIVEN get upcoming movies WHEN invoke is called THEN emit Resource Success`() =
        runTest {
            // Given
            val expectedData = Resource.Success(MockUtils.mockUpcomingMoviesEntity())
            coEvery { repository.getUpcomingMovies() } returns
                flow {
                    emit(expectedData)
                }

            // When
            val result = useCase.invoke()

            // Then
            coVerify(exactly = 1) {
                repository.getUpcomingMovies()
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
            val exception = Exception("Error fetching upcoming movies")
            val resourceError = getResourceError<ImmutableList<UpcomingMoviesEntity>>(exception)

            coEvery { repository.getUpcomingMovies() } returns
                flow {
                    emit(resourceError)
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertTrue {
                result.first() is Resource.Error<ImmutableList<UpcomingMoviesEntity>>
            }
            assertEquals(
                "Error fetching upcoming movies",
                (result.first() as Resource.Error).error.message,
            )
        }

    @Test
    fun `GIVEN no upcoming movies WHEN invoke is called THEN emit Resource_Success with empty list`() =
        runTest {
            // GIVEN
            coEvery { repository.getUpcomingMovies() } returns
                flow {
                    emit(Resource.Success(persistentListOf()))
                }

            // WHEN
            val result = useCase.invoke()

            // THEN
            assertEquals(Resource.Success(persistentListOf<UpcomingMoviesEntity>()), result.first())
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
