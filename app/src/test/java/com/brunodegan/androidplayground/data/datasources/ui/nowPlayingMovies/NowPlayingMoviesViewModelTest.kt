package com.brunodegan.androidplayground.data.datasources.ui.nowPlayingMovies

import app.cash.turbine.test
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.base.ui.SnackbarUiStateHolder
import com.brunodegan.androidplayground.domain.addToFavorites.AddToFavoritesUseCase
import com.brunodegan.androidplayground.domain.getNowPlaying.GetNowPlayingUseCase
import com.brunodegan.androidplayground.domain.model.FavoriteResult
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.removeFromFavorites.RemoveFromFavoritesUseCase
import com.brunodegan.androidplayground.testfixtures.MockUtils.getResourceError
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockFavoriteResult
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockMovieUiModels
import com.brunodegan.androidplayground.testfixtures.MockUtils.mockNowPlayingMovies
import com.brunodegan.androidplayground.testfixtures.TestDispatcherRule
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.actions.NowPlayingMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.state.NowPlayingMoviesUiState
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.viewModel.NowPlayingMoviesViewModel
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

class NowPlayingMoviesViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    private val getNowPlayingUseCase: GetNowPlayingUseCase = mockk()
    private val addToFavoritesUseCase: AddToFavoritesUseCase = mockk()
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase = mockk()

    private lateinit var viewModel: NowPlayingMoviesViewModel

    @Before
    fun setup() {
        viewModel =
            NowPlayingMoviesViewModel(
                useCase = getNowPlayingUseCase,
                addToFavoritesUseCase = addToFavoritesUseCase,
                removeFromFavoritesUseCase = removeFromFavoritesUseCase,
                dispatcher = testDispatcher,
                movieUiModelMapper = MovieUiModelMapper(),
                errorUiModelMapper = ErrorUiModelMapper(),
            )
    }

    @Test
    fun `GIVEN view model collects view entity succssefully WEN getNowPlayingMovies is invoked THEN asserts correct states is emitted`() =
        runTest {
            val mockData = mockNowPlayingMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getNowPlayingUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(NowPlayingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(NowPlayingMoviesUiState.Initial, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Loading, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN view model collects view entity with error state propagated WEN getNowPlayingMovies is invoked THEN asserts business error occurs`() =
        runTest {
            val exception = Exception("Error retrieving upComing movies")
            val resourceError = getResourceError<ImmutableList<Movie>>(exception)

            coEvery { getNowPlayingUseCase.invoke() } returns flow { emit(resourceError) }

            viewModel.onUiAction(NowPlayingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(NowPlayingMoviesUiState.Initial, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Loading, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Error(ErrorUiModel(resourceError.error.message)), awaitItem())
            }
        }

    @Test
    fun `GIVEN system error WHEN getNowPlayingMovies is invoked THEN asserts flow emits SnackbarUiState`() =
        runTest {
            val errorMsg = "message"
            val systemError = Throwable(errorMsg)

            coEvery { getNowPlayingUseCase.invoke() } returns flow { throw systemError }

            viewModel.onUiAction(NowPlayingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.snackbarState.test {
                assertEquals(SnackbarUiStateHolder.SnackbarUi(errorMsg), awaitItem())
            }
        }

    @Test
    fun `GIVEN previous error happens and users clicks onRetry button WHEN viewModel receives OnRetryButtonClickedUiAction THEN asserts successfully state occurs`() =
        runTest {
            val mockData = mockNowPlayingMovies()
            val successfullyResponse = Resource.Success(mockData)

            coEvery { getNowPlayingUseCase.invoke() } returns flow { emit(successfullyResponse) }

            viewModel.onUiAction(action = NowPlayingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(NowPlayingMoviesUiState.Initial, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Loading, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())
            }
        }

    @Test
    fun `GIVEN success now playing movie data WHEN OnAddFavButtonClickedUiAction is dispatched THEN asserts Initial and loading state and snackbar ui event is called`() =
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
                    NowPlayingMoviesUiActions.OnAddFavButtonClickedUiAction(
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
    fun `GIVEN success remove from now playing movies data WHEN OnRemoveFavButtonClickedUiAction is dispatched THEN asserts Initial and loading state and snackbar state is propagated`() =
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
                    NowPlayingMoviesUiActions.OnRemoveFavButtonClickedUiAction(
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
    fun `GIVEN stream throws WHEN retry is dispatched THEN uiState ends in Error instead of staying Loading`() =
        runTest {
            coEvery { getNowPlayingUseCase.invoke() } returns flow { throw Throwable("boom") }

            viewModel.onUiAction(NowPlayingMoviesUiActions.OnRetryButtonClickedUiAction)

            viewModel.uiState.test {
                assertEquals(NowPlayingMoviesUiState.Initial, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Loading, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Error(ErrorUiModel("boom")), awaitItem())
            }
        }

    @Test
    fun `GIVEN loaded list WHEN add favorite fails THEN snackbar is shown and list state is kept`() =
        runTest {
            val mockData = mockNowPlayingMovies()
            val error = getResourceError<FavoriteResult>(Exception("add failed"))
            coEvery { getNowPlayingUseCase.invoke() } returns flow { emit(Resource.Success(mockData)) }
            coEvery { addToFavoritesUseCase.invoke(1) } returns flow { emit(error) }

            viewModel.uiState.test {
                assertEquals(NowPlayingMoviesUiState.Initial, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Loading, awaitItem())
                assertEquals(NowPlayingMoviesUiState.Success(mockMovieUiModels(mockData)), awaitItem())

                viewModel.onUiAction(NowPlayingMoviesUiActions.OnAddFavButtonClickedUiAction(1))

                viewModel.snackbarState.test {
                    assertEquals(SnackbarUiStateHolder.SnackbarUi(error.error.message.orEmpty()), awaitItem())
                }
                expectNoEvents()
            }
        }

    @After
    fun tearDown() = unmockkAll()
}
