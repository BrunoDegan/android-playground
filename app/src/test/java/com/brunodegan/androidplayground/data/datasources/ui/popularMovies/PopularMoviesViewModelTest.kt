package com.brunodegan.androidplayground.data.datasources.ui.popularMovies

import app.cash.turbine.test
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.ui.SnackbarUiStateHolder
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getPopular.GetPopularUseCase
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockFavoriteResult
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMovieUiModels
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockPopularMovies
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.screen.popularMovies.actions.PopularMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.popularMovies.state.PopularMoviesUiState
import com.brunodegan.androidplayground.ui.screen.popularMovies.viewModel.PopularMoviesViewModel
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

class PopularMoviesViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    private val getPopularUseCase: GetPopularUseCase = mockk()
    private val addToFavoritesUseCase: AddToFavoritesUseCase = mockk()
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase = mockk()

    private lateinit var viewModel: PopularMoviesViewModel

    @Before
    fun setup() {
        viewModel =
            PopularMoviesViewModel(
                useCase = getPopularUseCase,
                addToFavoritesUseCase = addToFavoritesUseCase,
                removeFromFavoritesUseCase = removeFromFavoritesUseCase,
                dispatcher = testDispatcher,
                movieUiModelMapper = MovieUiModelMapper(),
                errorUiModelMapper = ErrorUiModelMapper(),
            )
    }

    @Test
    fun `GIVEN view model collects view entity succssefully WEN getPopularMovies is invoked THEN asserts correct states is emitted`() =
        runTest {
            val mockData = mockPopularMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getPopularUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(PopularMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(PopularMoviesUiState.Initial, awaitItem())
                assertEquals(PopularMoviesUiState.Loading, awaitItem())
                assertEquals(PopularMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN view model collects view entity with error state propagated WEN getPopularMovies is invoked THEN asserts business error occurs`() =
        runTest {
            val exception = Exception("Error retrieving upComing movies")
            val resourceError = getResourceError<ImmutableList<Movie>>(exception)

            coEvery { getPopularUseCase.invoke() } returns flow { emit(resourceError) }

            viewModel.onUiAction(PopularMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(PopularMoviesUiState.Initial, awaitItem())
                assertEquals(PopularMoviesUiState.Loading, awaitItem())
                assertEquals(PopularMoviesUiState.Error(ErrorUiModel(resourceError.error.message)), awaitItem())
            }
        }

    @Test
    fun `GIVEN system error WHEN getPopularMovies is invoked THEN asserts flow emits SnackbarUiState`() =
        runTest {
            val errorMsg = "message"
            val systemError = Throwable(errorMsg)

            coEvery { getPopularUseCase.invoke() } returns flow { throw systemError }

            viewModel.onUiAction(PopularMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(errorMsg), awaitItem())
            }
        }

    @Test
    fun `GIVEN previous error happens and users clicks onRetry button WHEN viewModel receives OnRetryButtonClickedUiAction THEN asserts successfully state occurs`() =
        runTest {
            val mockData = mockPopularMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getPopularUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(action = PopularMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(PopularMoviesUiState.Initial, awaitItem())
                assertEquals(PopularMoviesUiState.Loading, awaitItem())
                assertEquals(PopularMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
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
                    PopularMoviesUiActions.OnAddFavButtonClickedUiAction(
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
                    PopularMoviesUiActions.OnRemoveFavButtonClickedUiAction(
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
