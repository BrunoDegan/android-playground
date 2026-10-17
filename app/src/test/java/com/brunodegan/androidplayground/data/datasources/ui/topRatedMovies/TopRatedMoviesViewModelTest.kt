package com.brunodegan.androidplayground.data.datasources.ui.topRatedMovies

import app.cash.turbine.test
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.ui.SnackbarUiStateHolder
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getTopRated.GetTopRatedUseCase
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockFavoriteResult
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMovieUiModels
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockTopRatedMovies
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.actions.TopRatedMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.state.TopRatedMoviesUiState
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.viewModel.TopRatedMoviesViewModel
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

class TopRatedMoviesViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    private val getTopRatedUseCase: GetTopRatedUseCase = mockk()
    private val addToFavoritesUseCase: AddToFavoritesUseCase = mockk()
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase = mockk()

    private lateinit var viewModel: TopRatedMoviesViewModel

    @Before
    fun setup() {
        viewModel =
            TopRatedMoviesViewModel(
                useCase = getTopRatedUseCase,
                addToFavoritesUseCase = addToFavoritesUseCase,
                removeFromFavoritesUseCase = removeFromFavoritesUseCase,
                dispatcher = testDispatcher,
                movieUiModelMapper = MovieUiModelMapper(),
                errorUiModelMapper = ErrorUiModelMapper(),
            )
    }

    @Test
    fun `GIVEN view model collects view entity succssefully WEN getTopRatedMovies is invoked THEN asserts correct states is emitted`() =
        runTest {
            val mockData = mockTopRatedMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getTopRatedUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(TopRatedMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(TopRatedMoviesUiState.Initial, awaitItem())
                assertEquals(TopRatedMoviesUiState.Loading, awaitItem())
                assertEquals(TopRatedMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN view model collects view entity with error state propagated WEN getTopRatedMovies is invoked THEN asserts business error occurs`() =
        runTest {
            val exception = Exception("Error retrieving upComing movies")
            val resourceError = getResourceError<ImmutableList<Movie>>(exception)

            coEvery { getTopRatedUseCase.invoke() } returns flow { emit(resourceError) }

            viewModel.onUiAction(TopRatedMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(TopRatedMoviesUiState.Initial, awaitItem())
                assertEquals(TopRatedMoviesUiState.Loading, awaitItem())
                assertEquals(TopRatedMoviesUiState.Error(ErrorUiModel(resourceError.error.message)), awaitItem())
            }
        }

    @Test
    fun `GIVEN system error WHEN getTopRatedMovies is invoked THEN asserts flow emits SnackbarUiState`() =
        runTest {
            val errorMsg = "message"
            val systemError = Throwable(errorMsg)

            coEvery { getTopRatedUseCase.invoke() } returns flow { throw systemError }

            viewModel.onUiAction(TopRatedMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(errorMsg), awaitItem())
            }
        }

    @Test
    fun `GIVEN previous error happens and users clicks onRetry button WHEN viewModel receives OnRetryButtonClickedUiAction THEN asserts successfully state occurs`() =
        runTest {
            val mockData = mockTopRatedMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getTopRatedUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(action = TopRatedMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(TopRatedMoviesUiState.Initial, awaitItem())
                assertEquals(TopRatedMoviesUiState.Loading, awaitItem())
                assertEquals(TopRatedMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN success now top rated movie data WHEN OnAddFavButtonClickedUiAction is dispatched THEN asserts Initial and loading state and snackbar ui event is called`() =
        runTest {
            val mockAddFavoriteData = mockFavoriteResult()
            val successfullyResponse = Resource.Success(mockAddFavoriteData)
            val movieId = 1

            coEvery { addToFavoritesUseCase.invoke(movieId) } returns
                flow {
                    emit(
                        successfullyResponse,
                    )
                }

            viewModel.onUiAction(
                action =
                    TopRatedMoviesUiActions.OnAddFavButtonClickedUiAction(
                        movieId,
                    ),
            )

            viewModel.snackbarState.test {
                assertEquals(
                    SnackbarUiStateHolder.SnackbarUi(mockAddFavoriteData.statusMessage),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `GIVEN success remove from top rated movies data WHEN OnRemoveFavButtonClickedUiAction is dispatched THEN asserts Initial and loading state and snackbar state is propagated`() =
        runTest {
            val mockAddFavoriteData = mockFavoriteResult()
            val successfullyResponse = Resource.Success(mockAddFavoriteData)
            val movieId = 1

            coEvery { removeFromFavoritesUseCase.invoke(movieId) } returns
                flow {
                    emit(successfullyResponse)
                }

            viewModel.onUiAction(
                action =
                    TopRatedMoviesUiActions.OnRemoveFavButtonClickedUiAction(
                        movieId,
                    ),
            )

            viewModel.snackbarState.test {
                assertEquals(
                    SnackbarUiStateHolder.SnackbarUi(mockAddFavoriteData.statusMessage),
                    awaitItem(),
                )
            }
        }

    @After
    fun tearDown() = unmockkAll()
}
