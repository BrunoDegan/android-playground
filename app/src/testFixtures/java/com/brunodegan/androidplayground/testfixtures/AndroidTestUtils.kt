package com.brunodegan.androidplayground.testfixtures

import com.brunodegan.androidplayground.data.datasources.local.entities.FavoriteMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.PopularMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.TopRatedMoviesEntity
import com.brunodegan.androidplayground.data.datasources.local.entities.UpcomingMoviesEntity
import com.brunodegan.androidplayground.data.mappers.movies.MoviesEntityMapper
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

object AndroidTestUtils {
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
                voteAverage = 8,
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
                voteAverage = 7,
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
                voteAverage = 8,
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
                title = "Popular Movie 1",
                posterPath = "/mock/upcoming1.jpg",
                overview = "Overview for Upcoming Movie 1",
                originalLanguage = "de",
                popularity = 6.5,
                voteAverage = 8,
                releaseDate = "2023-07-01",
                isFavorite = true,
            ),
            PopularMoviesEntity(
                id = 2,
                title = "Popular Movie 2",
                posterPath = "/mock/upcoming2.jpg",
                overview = "Overview for Upcoming Movie 2",
                originalLanguage = "ja",
                popularity = 7.2,
                voteAverage = 7,
                releaseDate = "2023-08-01",
                isFavorite = true,
            ),
        )

    fun mockFavoriteMovieUiModels(): ImmutableList<MovieUiModel> = mockFavoriteMovies().map { MovieUiModelMapper().map(it) }.toImmutableList()

    fun mockTopRatedMovieUiModels(): ImmutableList<MovieUiModel> = mockTopRatedMovies().map { MovieUiModelMapper().map(it) }.toImmutableList()

    fun mockNowPlayingMovieUiModels(): ImmutableList<MovieUiModel> = mockNowPlayingMovies().map { MovieUiModelMapper().map(it) }.toImmutableList()

    fun mockUpcomingMovieUiModels(): ImmutableList<MovieUiModel> = mockUpcomingMovies().map { MovieUiModelMapper().map(it) }.toImmutableList()

    fun mockPopularMovieUiModels(): ImmutableList<MovieUiModel> = mockPopularMovies().map { MovieUiModelMapper().map(it) }.toImmutableList()

    fun mockFavoriteMovies(): ImmutableList<Movie> = mockFavoriteMoviesEntity().map { MoviesEntityMapper().map(it) }.toImmutableList()

    fun mockTopRatedMovies(): ImmutableList<Movie> = mockTopRatedMoviesEntity().map { MoviesEntityMapper().map(it) }.toImmutableList()

    fun mockNowPlayingMovies(): ImmutableList<Movie> = mockNowPlayingMoviesEntity().map { MoviesEntityMapper().map(it) }.toImmutableList()

    fun mockUpcomingMovies(): ImmutableList<Movie> = mockUpcomingMoviesEntity().map { MoviesEntityMapper().map(it) }.toImmutableList()

    fun mockPopularMovies(): ImmutableList<Movie> = mockPopularMoviesEntity().map { MoviesEntityMapper().map(it) }.toImmutableList()
}
