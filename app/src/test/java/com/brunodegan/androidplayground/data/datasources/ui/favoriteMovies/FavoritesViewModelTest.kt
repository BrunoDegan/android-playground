package com.brunodegan.androidplayground.data.datasources.ui.favoriteMovies

import app.cash.turbine.test
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.ui.SnackbarUiStateHolder
import com.brunodegan.androidplayground.domain.getFavorites.GetFavoritesUseCase
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockFavoriteMovies
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMovieUiModels
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.actions.FavoriteMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.state.FavoriteMoviesUiState
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.viewModel.FavoritesViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class FavoritesViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    private val useCase: GetFavoritesUseCase = mockk()

    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setup() {
        viewModel =
            FavoritesViewModel(
                useCase = useCase,
                movieUiModelMapper = MovieUiModelMapper(),
                errorUiModelMapper = ErrorUiModelMapper(),
                dispatcher = testDispatcher,
            )
    }

    @Test
    fun `GIVEN view model collects view entity succssefully WEN getFavoriteMovies is invoked THEN asserts correct states is emitted`() =
        runTest {
            val mockData = mockFavoriteMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { useCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(FavoriteMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(FavoriteMoviesUiState.Initial, awaitItem())
                assertEquals(FavoriteMoviesUiState.Loading, awaitItem())
                assertEquals(FavoriteMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN view model collects view entity with error state propagated WEN getFavoriteMovies is invoked THEN asserts business error occurs`() =
        runTest {
            val exception = Exception("Error retrieving favorites movies")
            val resourceError = getResourceError<ImmutableList<Movie>>(exception)

            coEvery { useCase.invoke() } returns flow { emit(resourceError) }

            viewModel.onUiAction(FavoriteMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(FavoriteMoviesUiState.Initial, awaitItem())
                assertEquals(FavoriteMoviesUiState.Loading, awaitItem())
                assertEquals(FavoriteMoviesUiState.Error(ErrorUiModel(resourceError.error.message)), awaitItem())
            }
        }

    @Test
    fun `GIVEN system error WHEN getFavoriteMovies is invoked THEN asserts flow emits SnackbarUiState`() =
        runTest {
            val errorMsg = "message"
            val systemError = Throwable(errorMsg)

            coEvery { useCase.invoke() } returns flow { throw systemError }

            viewModel.onUiAction(FavoriteMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(errorMsg), awaitItem())
            }
        }

    @Test
    fun `GIVEN previous error happens and users clicks onRetry button WHEN viewModel receives OnRetryButtonClickedUiAction THEN asserts successfully state occurs`() =
        runTest {
            val mockData = mockFavoriteMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { useCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(action = FavoriteMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(FavoriteMoviesUiState.Initial, awaitItem())
                assertEquals(FavoriteMoviesUiState.Loading, awaitItem())
                assertEquals(FavoriteMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @After
    fun tearDown() = unmockkAll()
}
