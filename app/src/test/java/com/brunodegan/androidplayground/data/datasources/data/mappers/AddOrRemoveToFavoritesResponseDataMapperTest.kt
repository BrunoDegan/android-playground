package com.brunodegan.androidplayground.data.datasources.data.mappers

import com.brunodegan.androidplayground.data.mappers.favorites.AddOrRemoveToFavoritesResponseDataMapper
import com.brunodegan.androidplayground.testfixtures.MockUtils
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddOrRemoveToFavoritesResponseDataMapperTest {
    private lateinit var mapper: AddOrRemoveToFavoritesResponseDataMapper

    @Before
    fun setup() {
        mapper = AddOrRemoveToFavoritesResponseDataMapper()
    }

    @Test
    fun `GIVEN mock add to favorites response WHEN map THEN asserts equality`() {
        val response = MockUtils.mockAddToFavoritesApiResponse()

        val result = mapper.map(response)

        assertTrue(result.success)
        assert(result.statusCode == "200")
        assert(result.statusMessage == "Movie added to favorites successfully")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
