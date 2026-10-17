package com.brunodegan.androidplayground.data.datasources.data.mappers

import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.testfixtures.MockUtils
import com.brunodegan.androidplayground.ui.mappers.ErrorUiModelMapper
import com.brunodegan.androidplayground.ui.mappers.MovieUiModelMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import org.junit.Test
import kotlin.test.assertEquals

class UiModelMappersTest {
    @Test
    fun `GIVEN domain movie WHEN MovieUiModelMapper maps THEN every field is carried over`() {
        val movie = MockUtils.mockNowPlayingMovies().first()

        assertEquals(
            expected =
                MovieUiModel(
                    id = movie.id,
                    title = movie.title,
                    posterPath = movie.posterPath,
                    overview = movie.overview,
                    releaseDate = movie.releaseDate,
                    originalLanguage = movie.originalLanguage,
                    popularity = movie.popularity,
                    voteAverage = movie.voteAverage,
                    isFavorite = movie.isFavorite,
                ),
            actual = MovieUiModelMapper().map(movie),
        )
    }

    @Test
    fun `GIVEN generic error WHEN ErrorUiModelMapper maps THEN message is kept`() {
        assertEquals(ErrorUiModel("boom"), ErrorUiModelMapper().map(ErrorType.Generic("boom")))
        assertEquals(ErrorUiModel(null), ErrorUiModelMapper().map(ErrorType.Generic(null)))
    }
}
