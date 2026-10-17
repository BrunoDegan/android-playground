package com.brunodegan.androidplayground.ui.screen.topRatedMovies

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
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
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.actions.TopRatedMoviesUiActions
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.state.TopRatedMoviesUiState
import com.brunodegan.androidplayground.ui.screen.topRatedMovies.viewModel.TopRatedMoviesViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

private const val SCREEN_NAME = "TopRelatedVideosScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopRatedVideosScreen(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    listState: LazyListState,
    onShowSnackbar: (String) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val viewModelStore = rememberViewModelStoreOwner()
    val viewModel: TopRatedMoviesViewModel = koinViewModel(viewModelStoreOwner = viewModelStore)
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

    TopRatedVideosScreenContent(
        scrollBehavior = scrollBehavior,
        state = uiState,
        listState = listState,
        modifier = modifier,
    ) {
        viewModel.onUiAction(action = it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopRatedVideosScreenContent(
    scrollBehavior: TopAppBarScrollBehavior,
    state: TopRatedMoviesUiState,
    modifier: Modifier,
    listState: LazyListState,
    onAction: (TopRatedMoviesUiActions) -> Unit,
) {
    when (state) {
        is TopRatedMoviesUiState.Initial -> {
            TrackScreen(screenName = SCREEN_NAME)
        }

        is TopRatedMoviesUiState.Success -> {
            SuccessState(
                scrollBehavior = scrollBehavior,
                listState = listState,
                modifier = modifier,
                viewData = state.viewData,
                onFavoriteButtonClicked = {
                    onAction(TopRatedMoviesUiActions.OnAddFavButtonClickedUiAction(it))
                },
                onRemoveFavButtonClickedUiAction = {
                    onAction(TopRatedMoviesUiActions.OnRemoveFavButtonClickedUiAction(it))
                },
            )
        }

        is TopRatedMoviesUiState.Error -> {
            ErrorUiState(
                modifier = modifier,
                errorData = state.error,
            ) {
                onAction(TopRatedMoviesUiActions.OnRetryButtonClickedUiAction)
            }
        }

        is TopRatedMoviesUiState.Loading -> {
            LoaderUiState()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessState(
    scrollBehavior: TopAppBarScrollBehavior,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    viewData: ImmutableList<MovieUiModel>,
    onFavoriteButtonClicked: (Int) -> Unit,
    onRemoveFavButtonClickedUiAction: (Int) -> Unit,
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
        items(viewData.size, key = { index ->
            "${viewData[index].id} -  ${viewData[index].title}"
        }) { position ->
            TopRatedMoviesCard(
                viewData = viewData[position],
                onFavoriteButtonClicked = {
                    onFavoriteButtonClicked(it)
                },
                onRemoveFavButtonClickedUiAction = {
                    onRemoveFavButtonClickedUiAction(it)
                },
            )
        }
    }
}

@Composable
private fun TopRatedMoviesCard(
    modifier: Modifier = Modifier,
    viewData: MovieUiModel,
    onFavoriteButtonClicked: (Int) -> Unit,
    onRemoveFavButtonClickedUiAction: (Int) -> Unit,
) {
    var isFavoriteButtonClicked by rememberSaveable { mutableStateOf(viewData.isFavorite) }

    val imageRequest =
        ImageRequest
            .Builder(LocalContext.current)
            .memoryCacheKey(viewData.title)
            .diskCacheKey(viewData.id.toString())
            .data(viewData.posterPath)
            .decoderFactory(SvgDecoder.Factory())
            .scale(Scale.FIT)
            .crossfade(true)
            .placeholder(R.drawable.movie_icon)
            .error(R.drawable.error_img)
            .build()

    val cardColor by animateColorAsState(
        targetValue =
            if (isFavoriteButtonClicked) {
                MaterialTheme.colorScheme.inversePrimary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        animationSpec =
            spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioHighBouncy,
            ),
        label = "animation",
    )
    Card(
        colors = CardDefaults.elevatedCardColors(cardColor),
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
                .testTag(stringResource(R.string.top_rated_movies_card_tag) + " " + viewData.id),
    ) {
        Image(
            painter =
                painterResource(
                    if (isFavoriteButtonClicked) {
                        R.drawable.added_to_favorites
                    } else {
                        R.drawable.not_added_to_favorites
                    },
                ),
            contentDescription = stringResource(R.string.add_to_favorites) + " " + viewData.id,
            modifier =
                Modifier
                    .align(Alignment.End)
                    .size(dimensionResource(R.dimen.favorite_icon_size))
                    .padding(top = dimensionResource(R.dimen.double_padding))
                    .clickable {
                        isFavoriteButtonClicked = isFavoriteButtonClicked.not()
                        if (isFavoriteButtonClicked) {
                            onFavoriteButtonClicked(viewData.id)
                        } else {
                            onRemoveFavButtonClickedUiAction(viewData.id)
                        }
                    },
        )
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
                    contentDescription = stringResource(R.string.top_rated_movies) + " " + viewData.id,
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
                modifier = Modifier.padding(dimensionResource(R.dimen.base_padding)),
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
                    stringResource(R.string.movie_language).format(
                        viewData.originalLanguage.toUpperCase(Locale.current),
                    ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize =
                    TextUnit(
                        value =
                            ResourcesCompat.getFloat(
                                LocalResources.current,
                                R.dimen.movie_language_font_size,
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
                                R.dimen.movie_language_font_size,
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
                    stringResource(R.string.movie_popularity).format(
                        viewData.popularity.toString(),
                    ),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
                fontSize =
                    TextUnit(
                        value =
                            ResourcesCompat.getFloat(
                                LocalResources.current,
                                R.dimen.movie_language_font_size,
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

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.movie_vote_average),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    fontSize =
                        TextUnit(
                            value =
                                ResourcesCompat.getFloat(
                                    LocalResources.current,
                                    R.dimen.movie_language_font_size,
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

                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier =
                        Modifier.padding(
                            top = dimensionResource(R.dimen.base_padding),
                            bottom = dimensionResource(R.dimen.double_padding),
                        ),
                ) {
                    items(viewData.voteAverage) { _ ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(R.string.movie_vote_average),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier.padding(
                                    start = dimensionResource(R.dimen.small_padding),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopRatedMoviesScreen() {
    TopRatedVideosScreenContent(
        listState = rememberLazyListState(),
        state =
            TopRatedMoviesUiState.Success(
                viewData =
                    persistentListOf(
                        MovieUiModel(
                            id = 0,
                            title = "title",
                            posterPath = "posterPath",
                            overview = "overview",
                            originalLanguage = "originalLanguage",
                            popularity = 10.0,
                            voteAverage = 7,
                            releaseDate = "24/04/2025",
                            isFavorite = false,
                        ),
                    ),
            ),
        onAction = {},
        modifier = Modifier,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    )
}
