package com.brunodegan.androidplayground.data.mappers

import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoriteMoviesData
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesApiResponse
import org.koin.core.annotation.Factory

@Factory
class AddOrRemoveToFavoritesResponseDataMapper : BaseMapper<AddToFavoritesApiResponse, AddToFavoriteMoviesData> {
    override fun map(input: AddToFavoritesApiResponse): AddToFavoriteMoviesData =
        AddToFavoriteMoviesData(
            success = input.success,
            statusMessage = input.statusMessage,
            statusCode = input.statusCode.toString(),
        )
}
