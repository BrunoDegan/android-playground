package com.brunodegan.androidplayground.testfixtures

import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoriteMoviesData
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesApiResponse
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesRequest
import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.Movies
import com.brunodegan.androidplayground.data.datasources.local.entities.MoviesApiDataResponse
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json

object MockUtils {
    const val MOVIES_POSTER_CDN_URL = "https://image.tmdb.org/t/p/original"

    fun mockMoviesApiDataResponse(): MoviesApiDataResponse =
        MoviesApiDataResponse(
            results =
                listOf(
                    Movies(
                        id = 1,
                        title = "Mock Movie 1",
                        posterPath = "/mock/path1.jpg",
                        overview = "Overview for Mock Movie 1",
                        originalLanguage = "en",
                        popularity = 8.5,
                        voteAverage = 7.8,
                        releaseDate = "2023-01-01",
                    ),
                    Movies(
                        id = 2,
                        title = "Mock Movie 2",
                        posterPath = "/mock/path2.jpg",
                        overview = "Overview for Mock Movie 2",
                        originalLanguage = "pt",
                        popularity = 9.0,
                        voteAverage = 8.2,
                        releaseDate = "2023-02-01",
                    ),
                ),
        )

    fun mockFavoriteMoviesEntity(): ImmutableList<FavoriteMoviesEntity> =
        persistentListOf(
            FavoriteMoviesEntity(
                id = 1,
                title = "Favorite Movie 1",
                posterPath = "/mock/favorite1.jpg",
                overview = "Overview for Favorite Movie 1",
                releaseDate = "2023-01-01",
                lastUpdated = System.currentTimeMillis(),
            ),
            FavoriteMoviesEntity(
                id = 2,
                title = "Favorite Movie 2",
                posterPath = "/mock/favorite2.jpg",
                overview = "Overview for Favorite Movie 2",
                releaseDate = "2023-02-01",
                lastUpdated = System.currentTimeMillis(),
            ),
        )

    fun mockTopRatedMoviesEntity(): ImmutableList<TopRatedMoviesEntity> =
        persistentListOf(
            TopRatedMoviesEntity(
                id = 1,
                title = "Top Rated Movie 1",
                posterPath = "/mock/toprated1.jpg",
                overview = "Overview for Top Rated Movie 1",
                originalLanguage = "en",
                popularity = 9.5,
                voteAverage = 9,
                releaseDate = "2023-03-01",
                isFavorite = true,
            ),
            TopRatedMoviesEntity(
                id = 2,
                title = "Top Rated Movie 2",
                posterPath = "/mock/toprated2.jpg",
                overview = "Overview for Top Rated Movie 2",
                originalLanguage = "fr",
                popularity = 8.8,
                voteAverage = 8,
                releaseDate = "2023-04-01",
                isFavorite = true,
            ),
        )

    fun mockNowPlayingMoviesEntity(): ImmutableList<NowPlayingMoviesEntity> =
        persistentListOf(
            NowPlayingMoviesEntity(
                id = 1,
                title = "Now Playing Movie 1",
                posterPath = "/mock/nowplaying1.jpg",
                overview = "Overview for Now Playing Movie 1",
                originalLanguage = "es",
                popularity = 7.5,
                voteAverage = 7,
                releaseDate = "2023-05-01",
                isFavorite = true,
            ),
            NowPlayingMoviesEntity(
                id = 2,
                title = "Now Playing Movie 2",
                posterPath = "/mock/nowplaying2.jpg",
                overview = "Overview for Now Playing Movie 2",
                originalLanguage = "it",
                popularity = 8.0,
                voteAverage = 8,
                releaseDate = "2023-06-01",
                isFavorite = true,
            ),
        )

    fun mockUpcomingMoviesEntity(): ImmutableList<UpcomingMoviesEntity> =
        persistentListOf(
            UpcomingMoviesEntity(
                id = 1,
                title = "Upcoming Movie 1",
                posterPath = "/mock/upcoming1.jpg",
                overview = "Overview for Upcoming Movie 1",
                originalLanguage = "de",
                popularity = 6.5,
                voteAverage = 7,
                releaseDate = "2023-07-01",
                isFavorite = true,
            ),
            UpcomingMoviesEntity(
                id = 2,
                title = "Upcoming Movie 2",
                posterPath = "/mock/upcoming2.jpg",
                overview = "Overview for Upcoming Movie 2",
                originalLanguage = "ja",
                popularity = 7.2,
                voteAverage = 7,
                releaseDate = "2023-08-01",
                isFavorite = true,
            ),
        )

    fun mockPopularMoviesEntity(): ImmutableList<PopularMoviesEntity> =
        persistentListOf(
            PopularMoviesEntity(
                id = 1,
                title = "Upcoming Movie 1",
                posterPath = "/mock/upcoming1.jpg",
                overview = "Overview for Upcoming Movie 1",
                originalLanguage = "de",
                popularity = 6.5,
                voteAverage = 7,
                releaseDate = "2023-07-01",
                isFavorite = true,
            ),
            PopularMoviesEntity(
                id = 2,
                title = "Upcoming Movie 2",
                posterPath = "/mock/upcoming2.jpg",
                overview = "Overview for Upcoming Movie 2",
                originalLanguage = "ja",
                popularity = 7.2,
                voteAverage = 7,
                releaseDate = "2023-08-01",
                isFavorite = true,
            ),
        )

    fun mockAddToFavoritesApiResponse(): AddToFavoritesApiResponse =
        AddToFavoritesApiResponse(
            success = true,
            statusMessage = "Movie added to favorites successfully",
            statusCode = 200,
        )

    fun mockAddToFavoritesRequest(): AddToFavoritesRequest =
        AddToFavoritesRequest(
            mediaType = "movie",
            mediaId = 1,
            favorite = true,
        )

    fun mockAddToFavoriteMoviesData(): AddToFavoriteMoviesData =
        AddToFavoriteMoviesData(
            success = true,
            statusMessage = "Movie added to favorites successfully",
            statusCode = "200",
        )

    inline fun <reified T> toJsonString(obj: T): String = Json.encodeToString(obj)

    fun <T> getResourceError(exception: Exception) = Resource.Error<T>(ErrorType.Generic(exception.message))
}
