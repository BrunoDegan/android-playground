package com.brunodegan.androidplayground.data.datasources.ui.upComingMovies

import app.cash.turbine.test
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.ui.SnackbarUiStateHolder
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getUpComing.GetUpComingUseCase
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockFavoriteResult
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMovieUiModels
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockUpcomingMovies
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.screen.upComingMovies.actions.UpComingMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.upComingMovies.state.UpComingMoviesUiState
import com.brunodegan.androidplayground.ui.screen.upComingMovies.viewModel.UpComingMoviesViewModel
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

class UpComingMoviesViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    private val getUpComingUseCase: GetUpComingUseCase = mockk()
    private val addToFavoritesUseCase: AddToFavoritesUseCase = mockk()
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase = mockk()

    private lateinit var viewModel: UpComingMoviesViewModel

    @Before
    fun setup() {
        viewModel =
            UpComingMoviesViewModel(
                useCase = getUpComingUseCase,
                addToFavoritesUseCase = addToFavoritesUseCase,
                removeFromFavoritesUseCase = removeFromFavoritesUseCase,
                dispatcher = testDispatcher,
                movieUiModelMapper = MovieUiModelMapper(),
                errorUiModelMapper = ErrorUiModelMapper(),
            )
    }

    @Test
    fun `GIVEN view model collects view entity succssefully WEN getUpComingUseCase is invoked THEN asserts correct states is emitted`() =
        runTest {
            val mockData = mockUpcomingMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getUpComingUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(UpComingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(UpComingMoviesUiState.Initial, awaitItem())
                assertEquals(UpComingMoviesUiState.Loading, awaitItem())
                assertEquals(UpComingMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN view model collects view entity with error state propagated WEN getUpComingUseCase is invoked THEN asserts business error occurs`() =
        runTest {
            val exception = Exception("Error retrieving upComing movies")
            val resourceError = getResourceError<ImmutableList<Movie>>(exception)

            coEvery { getUpComingUseCase.invoke() } returns flow { emit(resourceError) }

            viewModel.onUiAction(UpComingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(UpComingMoviesUiState.Initial, awaitItem())
                assertEquals(UpComingMoviesUiState.Loading, awaitItem())
                assertEquals(UpComingMoviesUiState.Error(ErrorUiModel(resourceError.error.message)), awaitItem())
            }
        }

    @Test
    fun `GIVEN system error WHEN getUpComingMovies is invoked THEN asserts flow emits SnackbarUiState`() =
        runTest {
            val errorMsg = "message"
            val systemError = Throwable(errorMsg)

            coEvery { getUpComingUseCase.invoke() } returns flow { throw systemError }

            viewModel.onUiAction(UpComingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(errorMsg), awaitItem())
            }
        }

    @Test
    fun `GIVEN previous error happens and users clicks onRetry button WHEN viewModel receives OnRetryButtonClickedUiAction THEN asserts successfully state occurs`() =
        runTest {
            val mockData = mockUpcomingMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getUpComingUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(action = UpComingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(UpComingMoviesUiState.Initial, awaitItem())
                assertEquals(UpComingMoviesUiState.Loading, awaitItem())
                assertEquals(UpComingMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN success add to favorite movie data WHEN OnAddFavButtonClickedUiAction is dispatched THEN asserts Initial and loading state and snackbar ui event is called`() =
        runTest {
            val mockData = mockFavoriteResult()
            val successfullyResponse = Resource.Success(mockData)
            val movieId = 1

            coEvery { addToFavoritesUseCase.invoke(id = movieId) } returns
                flow {
                    emit(
                        successfullyResponse,
                    )
                }

            viewModel.onUiAction(action = UpComingMoviesUiActions.OnAddFavButtonClickedUiAction(movieId))

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(mockData.statusMessage), awaitItem())
            }
        }

    @Test
    fun `GIVEN success remove from favorite movie data WHEN OnRemoveFavButtonClickedUiAction is dispatched THEN asserts Initial and loading state and snackbar state is propagated`() =
        runTest {
            val mockData = mockFavoriteResult()
            val successfullyResponse = Resource.Success(mockData)
            val movieId = 1

            coEvery { removeFromFavoritesUseCase.invoke(id = movieId) } returns
                flow {
                    emit(
                        successfullyResponse,
                    )
                }

            viewModel.onUiAction(
                action =
                    UpComingMoviesUiActions.OnRemoveFavButtonClickedUiAction(
                        movieId,
                    ),
            )

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(mockData.statusMessage), awaitItem())
            }
        }

    @After
    fun tearDown() = unmockkAll()
}
