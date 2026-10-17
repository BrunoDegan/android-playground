package com.brunodegan.androidplayground.ui.screen.favoriteMovies

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import com.brunodegan.androidplayground.R
import com.brunodegan.androidplayground.base.ui.ErrorUiState
import com.brunodegan.androidplayground.base.ui.LoaderUiState
import com.brunodegan.androidplayground.base.ui.ObserveAsEvent
import com.brunodegan.androidplayground.base.ui.PosterShape
import com.brunodegan.androidplayground.base.ui.SnackbarUiStateHolder
import com.brunodegan.androidplayground.data.metrics.TrackScreen
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.actions.FavoriteMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.state.FavoriteMoviesUiState
import com.brunodegan.androidplayground.ui.screen.favoriteMovies.viewModel.FavoritesViewModel
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

private const val SCREEN_NAME = "FavoriteMoviesScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FavoriteMoviesScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    onNavigateUp: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    val viewModelStore = rememberViewModelStoreOwner()
    val viewModel: FavoritesViewModel = koinViewModel(viewModelStoreOwner = viewModelStore)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        onNavigateUp()
    }

    ObserveAsEvent(flow = viewModel.snackbarState) { event ->
        when (event) {
            is SnackbarUiStateHolder.SnackbarUi -> {
                onShowSnackbar(event.msg)
            }
        }
    }

    FavoritesMoviesScreenContent(
        state = uiState,
        scrollBehavior = scrollBehavior,
        listState = listState,
        modifier = modifier,
    ) {
        viewModel.onUiAction(action = it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition(SCREEN_NAME)
@Composable
internal fun FavoritesMoviesScreenContent(
    scrollBehavior: TopAppBarScrollBehavior,
    listState: LazyListState,
    state: FavoriteMoviesUiState,
    modifier: Modifier,
    onAction: (FavoriteMoviesUiActions) -> Unit,
) {
    when (state) {
        is FavoriteMoviesUiState.Initial -> {
            TrackScreen(screenName = SCREEN_NAME)
        }

        is FavoriteMoviesUiState.Loading -> {
            LoaderUiState()
        }

        is FavoriteMoviesUiState.Success -> {
            SuccessState(
                modifier = modifier,
                listState = listState,
                scrollBehavior = scrollBehavior,
                viewData = state.viewData,
            )
        }

        is FavoriteMoviesUiState.Error -> {
            ErrorUiState(
                modifier = modifier,
                errorData = state.error,
            ) {
                onAction(FavoriteMoviesUiActions.OnRetryButtonClickedUiAction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessState(
    modifier: Modifier,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    viewData: ImmutableList<MovieUiModel>,
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        flingBehavior = ScrollableDefaults.flingBehavior(),
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
    ) {
        items(viewData.size, key = { index -> viewData[index].title }) { position ->
            FavoritesMoviesCard(
                viewData = viewData[position],
            )
        }
    }
}

@Composable
fun FavoritesMoviesCard(
    viewData: MovieUiModel,
    modifier: Modifier = Modifier,
) {
    val imageRequest =
        ImageRequest
            .Builder(LocalContext.current)
            .memoryCacheKey(viewData.title)
            .diskCacheKey(viewData.title)
            .data(viewData.posterPath)
            .decoderFactory(SvgDecoder.Factory())
            .scale(Scale.FIT)
            .crossfade(true)
            .placeholder(R.drawable.movie_icon)
            .error(R.drawable.error_img)
            .build()

    Card(
        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.primaryContainer),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.card_elevation)),
        border =
            BorderStroke(
                dimensionResource(R.dimen.card_border_elevation),
                MaterialTheme.colorScheme.tertiary,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = dimensionResource(R.dimen.card_padding))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .testTag(stringResource(R.string.favorites_movies_card_tag) + " " + viewData.id),
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier =
                Modifier
                    .padding(start = dimensionResource(R.dimen.double_padding))
                    .wrapContentSize(),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(R.dimen.double_padding),
                            bottom = dimensionResource(R.dimen.base_padding),
                        ),
            ) {
                AsyncImage(
                    model = imageRequest,
                    fallback = painterResource(R.drawable.movie_icon),
                    error = painterResource(R.drawable.error_img),
                    contentDescription = stringResource(R.string.favorites_movies) + " " + viewData.id,
                    filterQuality = FilterQuality.Low,
                    modifier =
                        Modifier
                            .size(
                                dimensionResource(R.dimen.movie_poster_size),
                                dimensionResource(R.dimen.movie_poster_size),
                            ).clip(PosterShape())
                            .fillMaxWidth(),
                )
            }
            Text(
                text = viewData.title,
                fontWeight = FontWeight.W600,
                fontSize =
                    TextUnit(
                        value =
                            ResourcesCompat.getFloat(
                                LocalResources.current,
                                R.dimen.movie_title_font_size,
                            ),
                        type = TextUnitType.Sp,
                    ),
                textAlign = TextAlign.Justify,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(
                        top = dimensionResource(R.dimen.double_padding),
                        start = dimensionResource(R.dimen.base_padding),
                    ),
            )
            Text(
                text = viewData.overview,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.W400,
                fontSize =
                    TextUnit(
                        value =
                            ResourcesCompat.getFloat(
                                LocalResources.current,
                                R.dimen.movie_overview_font_size,
                            ),
                        type = TextUnitType.Sp,
                    ),
                textAlign = TextAlign.Justify,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier.padding(
                        dimensionResource(R.dimen.base_padding),
                    ),
            )
            Text(
                text =
                    stringResource(R.string.movie_release_date).format(
                        viewData.releaseDate,
                    ),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
                fontSize =
                    TextUnit(
                        value =
                            ResourcesCompat.getFloat(
                                LocalResources.current,
                                R.dimen.movie_overview_font_size,
                            ),
                        type = TextUnitType.Sp,
                    ),
                textAlign = TextAlign.Justify,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier.padding(
                        dimensionResource(R.dimen.base_padding),
                    ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun PopularMoviesScreenPreview() {
    FavoritesMoviesScreenContent(
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        listState = rememberLazyListState(),
        state =
            FavoriteMoviesUiState.Success(
                viewData =
                    persistentListOf(
                        MovieUiModel(
                            id = 0,
                            title = "title",
                            posterPath = "posterPath",
                            overview = "overview",
                            releaseDate = "29/04/2025",
                        ),
                    ),
            ),
        onAction = {},
        modifier = Modifier,
    )
}
