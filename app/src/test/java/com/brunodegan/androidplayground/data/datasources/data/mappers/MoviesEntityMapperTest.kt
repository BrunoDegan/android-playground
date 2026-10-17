package com.brunodegan.androidplayground.data.datasources.data.mappers

import com.brunodegan.androidplayground.data.mappers.movies.MoviesEntityMapper
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.testfixtures.MockUtils
import org.junit.Test
import kotlin.test.assertEquals

class MoviesEntityMapperTest {
    private val mapper = MoviesEntityMapper()

    @Test
    fun `GIVEN now playing entity WHEN map THEN all fields land in Movie`() {
        val entity = MockUtils.mockNowPlayingMoviesEntity().first()
        val result = mapper.map(entity)

        assertEquals(
            expected =
                Movie(
                    id = entity.id,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    overview = entity.overview,
                    releaseDate = entity.releaseDate,
                    originalLanguage = entity.originalLanguage,
                    popularity = entity.popularity,
                    voteAverage = entity.voteAverage,
                    isFavorite = entity.isFavorite,
                ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN favorite entity WHEN map THEN all fields land in Movie`() {
        val entity = MockUtils.mockFavoriteMoviesEntity().first()
        val result = mapper.map(entity)

        assertEquals(
            expected =
                Movie(
                    id = entity.id,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    overview = entity.overview,
                    releaseDate = entity.releaseDate,
                    originalLanguage = entity.originalLanguage,
                    popularity = entity.popularity,
                    voteAverage = entity.voteAverage,
                    isFavorite = entity.isFavorite,
                ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN popular entity WHEN map THEN all fields land in Movie`() {
        val entity = MockUtils.mockPopularMoviesEntity().first()
        val result = mapper.map(entity)
        assertEquals(
            expected =
                Movie(
                    id = entity.id,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    overview = entity.overview,
                    releaseDate = entity.releaseDate,
                    originalLanguage = entity.originalLanguage,
                    popularity = entity.popularity,
                    voteAverage = entity.voteAverage,
                    isFavorite = entity.isFavorite,
                ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN top rated movies WHEN map THEN all fields land in Movie`() {
        val entity = MockUtils.mockTopRatedMoviesEntity().first()
        val result = mapper.map(entity)

        assertEquals(
            expected =
                Movie(
                    id = entity.id,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    overview = entity.overview,
                    releaseDate = entity.releaseDate,
                    originalLanguage = entity.originalLanguage,
                    popularity = entity.popularity,
                    voteAverage = entity.voteAverage,
                    isFavorite = entity.isFavorite,
                ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN every category entity WHEN map THEN same shared mapper handles it`() {
        val entities =
            listOf(
                MockUtils.mockPopularMoviesEntity().first(),
                MockUtils.mockTopRatedMoviesEntity().first(),
                MockUtils.mockUpcomingMoviesEntity().first(),
            )

        entities.forEach { assertEquals(expected = it.id, actual = mapper.map(it).id) }
    }
}
