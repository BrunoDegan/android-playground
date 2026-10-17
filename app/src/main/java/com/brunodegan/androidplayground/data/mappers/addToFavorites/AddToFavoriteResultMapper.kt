package com.brunodegan.androidplayground.data.mappers.addToFavorites

import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoriteMoviesData
import com.brunodegan.androidplayground.data.mappers.BaseMapper
import com.brunodegan.androidplayground.domain.model.FavoriteResult
import org.koin.core.annotation.Factory

@Factory
class AddToFavoriteResultMapper : BaseMapper<AddToFavoriteMoviesData, FavoriteResult> {
    override fun map(input: AddToFavoriteMoviesData): FavoriteResult = FavoriteResult(input.success, input.statusMessage, input.statusCode)
}
