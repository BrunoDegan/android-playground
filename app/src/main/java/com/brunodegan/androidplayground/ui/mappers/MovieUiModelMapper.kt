package com.brunodegan.androidplayground.ui.mappers

import com.brunodegan.androidplayground.data.mappers.BaseMapper
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.ui.model.MovieUiModel
import org.koin.core.annotation.Factory

@Factory
class MovieUiModelMapper : BaseMapper<Movie, MovieUiModel> {
    override fun map(input: Movie): MovieUiModel =
        MovieUiModel(
            id = input.id,
            title = input.title,
            posterPath = input.posterPath,
            overview = input.overview,
            releaseDate = input.releaseDate,
            originalLanguage = input.originalLanguage,
            popularity = input.popularity,
            voteAverage = input.voteAverage,
            isFavorite = input.isFavorite,
        )
}
