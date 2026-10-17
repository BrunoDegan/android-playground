package com.brunodegan.androidplayground.data.datasources.data.mappers

import com.brunodegan.androidplayground.base.utils.formatUsDateToBrDate
import com.brunodegan.androidplayground.data.mappers.topRated.TopRatedDataMapper
import com.brunodegan.androidplayground.testfixtures.MockUtils
import com.brunodegan.androidplayground.testfixtures.MockUtils.MOVIES_POSTER_CDN_URL
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class TopRatedMapperTest {
    private lateinit var mapper: TopRatedDataMapper

    @Before
    fun setup() {
        mapper = TopRatedDataMapper()
    }

    @Test
    fun `GIVEN mock add to favorites response WHEN map THEN asserts equality`() {
        val response = MockUtils.mockMoviesApiDataResponse()
        val result = mapper.map(response)

        assertEquals(expected = response.results.first().id, actual = result.first().id)
        assertEquals(expected = response.results.first().title, actual = result.first().title)
        assertEquals(expected = response.results.first().overview, actual = result.first().overview)
        assertEquals(
            expected =
                response.results
                    .first()
                    .releaseDate
                    .formatUsDateToBrDate(),
            actual = result.first().releaseDate,
        )
        assertEquals(
            expected = MOVIES_POSTER_CDN_URL + response.results.first().posterPath,
            actual = result.first().posterPath,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
