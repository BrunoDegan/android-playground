package com.brunodegan.androidplayground.data.datasources.domain.removeFromFavorites

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoriteMoviesData
import com.brunodegan.androidplayground.data.repositories.MoviesRepository
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCaseImpl
import com.brunodegan.androidplayground.testfixtures.MockUtils
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class RemoveFromFavoritesUseCaseTest {
    @get:Rule
    val mainDispatcher = TestDispatcherRule()

    private val repository: MoviesRepository = mockk()
    private lateinit var useCase: RemoveFromFavoritesUseCase
    private val movieId = 1

    @Before
    fun setUp() {
        useCase = RemoveFromFavoritesUseCaseImpl(repository = repository)
    }

    @Test
    fun `GIVEN favorite movies WHEN invoke is called THEN emit Resource_Success and call removeFavorite once`() =
        runTest {
            // Given
            val expectedData = Resource.Success(MockUtils.mockAddToFavoriteMoviesData())
            coEvery { repository.removeFavorite(id = movieId) } returns
                flow {
                    emit(expectedData)
                }

            // When
            val result = useCase.invoke(id = movieId)

            // Then
            coVerify(exactly = 1) {
                repository.removeFavorite(id = movieId)
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
            val exception = Exception("Error removing favorite movie")
            val resourceError = getResourceError<AddToFavoriteMoviesData>(exception)

            coEvery { repository.removeFavorite(id = movieId) } returns
                flow {
                    emit(resourceError)
                }

            // WHEN
            val result = useCase.invoke(id = movieId)

            // THEN
            assertTrue {
                result.first() is Resource.Error<AddToFavoriteMoviesData>
            }
            assertEquals(
                "Error removing favorite movie",
                (result.first() as Resource.Error).error.message,
            )
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
