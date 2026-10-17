package com.brunodegan.androidplayground.ui.screen

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.brunodegan.androidplayground.base.navigation.AppNavHost
import com.brunodegan.androidplayground.data.metrics.LocalMetrics
import com.brunodegan.androidplayground.testfixtures.AndroidTestUtils
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.FavoritesMoviesScreenContent
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.state.FavoriteMoviesUiState
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.NowPlayingMoviesScreenContent
import com.brunodegan.androidplayground.ui.screen.nowPlayingMovies.state.NowPlayingMoviesUiState
import com.brunodegan.androidplayground.ui.screen.popularMovies.PopularMoviesScreenContent
import com.brunodegan.androidplayground.ui.screen.popularMovies.state.PopularMoviesUiState
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.TopRatedVideosScreenContent
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.state.TopRatedMoviesUiState
import com.brunodegan.androidplayground.ui.screen.upComingMovies.UpComingMoviesScreenContent
import com.brunodegan.androidplayground.ui.screen.upComingMovies.state.UpComingMoviesUiState
import com.brunodegan.androidplayground.ui.theme.AndroidPlaygroundTheme
import org.junit.Rule
import org.junit.Test

class AppNavHostScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val bottomNavItem1: String = "Em cartaz"
    private val bottomNavItem2: String = "Por vir"
    private val bottomNavItem3: String = "Top Ranking"
    private val bottomNavItem4: String = "Populares"
    private val bottomNavItem5: String = "Favoritos"

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testNavHostAllItemsShowing() {
        composeTestRule.setContent {
            CompositionLocalProvider {
                LocalMetrics
                AndroidPlaygroundTheme {
                    AppNavHost()
                }
            }
        }

        composeTestRule.apply {
            onNodeWithText(bottomNavItem1).assertIsSelected()
        }

        composeTestRule.onNodeWithText(bottomNavItem1).assertExists()
        composeTestRule.onNodeWithText(bottomNavItem2).assertExists()
        composeTestRule.onNodeWithText(bottomNavItem3).assertExists()
        composeTestRule.onNodeWithText(bottomNavItem4).assertExists()
        composeTestRule.onNodeWithText(bottomNavItem5).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testUpComingScreenShowing() {
        composeTestRule.setContent {
            CompositionLocalProvider {
                AndroidPlaygroundTheme {
                    UpComingMoviesScreenContent(
                        listState = rememberLazyListState(),
                        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                        state =
                            UpComingMoviesUiState.Success(
                                viewData = AndroidTestUtils.mockUpcomingMovieUiModels(),
                            ),
                        onAction = {},
                        modifier = Modifier,
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Upcoming Movie 1")
            .assertExists()

        composeTestRule
            .onNodeWithText("Upcoming Movie 2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("upcoming movies card tag 1")
            .assertExists()

        composeTestRule
            .onNodeWithTag("upcoming movies card tag 2")
            .assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 1",
            ).assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 2",
            ).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testNowPlayingScreenShowing() {
        composeTestRule.setContent {
            CompositionLocalProvider {
                AndroidPlaygroundTheme {
                    NowPlayingMoviesScreenContent(
                        listState = rememberLazyListState(),
                        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                        state =
                            NowPlayingMoviesUiState.Success(
                                viewData = AndroidTestUtils.mockNowPlayingMovieUiModels(),
                            ),
                        onAction = {},
                        modifier = Modifier,
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Now Playing Movie 1")
            .assertExists()

        composeTestRule
            .onNodeWithText("Now Playing Movie 2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("now playing movies card tag 1")
            .assertExists()

        composeTestRule
            .onNodeWithTag("now playing movies card tag 2")
            .assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 1",
            ).assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 2",
            ).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testFavoritesScreenShowing() {
        composeTestRule.setContent {
            CompositionLocalProvider {
                AndroidPlaygroundTheme {
                    FavoritesMoviesScreenContent(
                        listState = rememberLazyListState(),
                        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                        state =
                            FavoriteMoviesUiState.Success(
                                viewData = AndroidTestUtils.mockFavoriteMovieUiModels(),
                            ),
                        onAction = {},
                        modifier = Modifier,
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Favorite Movie 1")
            .assertExists()

        composeTestRule
            .onNodeWithText("Favorite Movie 2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("favorites movies card tag 1")
            .assertExists()

        composeTestRule
            .onNodeWithTag("favorites movies card tag 2")
            .assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testPopularMoviesIsShowing() {
        composeTestRule.setContent {
            CompositionLocalProvider {
                AndroidPlaygroundTheme {
                    PopularMoviesScreenContent(
                        listState = rememberLazyListState(),
                        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                        state =
                            PopularMoviesUiState.Success(
                                viewData = AndroidTestUtils.mockPopularMovieUiModels(),
                            ),
                        onAction = {},
                        modifier = Modifier,
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Popular Movie 1")
            .assertExists()

        composeTestRule
            .onNodeWithText("Popular Movie 2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("popular movies card tag 1")
            .assertExists()

        composeTestRule
            .onNodeWithTag("popular movies card tag 2")
            .assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 1",
            ).assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 2",
            ).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testTopRatedMoviesIsShowing() {
        composeTestRule.setContent {
            CompositionLocalProvider {
                AndroidPlaygroundTheme {
                    TopRatedVideosScreenContent(
                        listState = rememberLazyListState(),
                        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                        state =
                            TopRatedMoviesUiState.Success(
                                viewData = AndroidTestUtils.mockTopRatedMovieUiModels(),
                            ),
                        onAction = {},
                        modifier = Modifier,
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Top Rated Movie 1")
            .assertExists()

        composeTestRule
            .onNodeWithText("Top Rated Movie 2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("top rated movies card tag 1")
            .assertExists()

        composeTestRule
            .onNodeWithTag("top rated movies card tag 2")
            .assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 1",
            ).assertExists()

        composeTestRule
            .onNodeWithContentDescription(
                "Adicionar aos favoritos 2",
            ).assertExists()
    }
}
