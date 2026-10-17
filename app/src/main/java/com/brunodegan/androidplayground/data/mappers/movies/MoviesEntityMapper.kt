package com.brunodegan.androidplayground.data.mappers.movies

import com.brunodegan.androidplayground.data.datasources.local.entities.MovieEntity
import com.brunodegan.androidplayground.data.mappers.BaseMapper
import com.brunodegan.androidplayground.domain.model.Movie
import org.koin.core.annotation.Factory

@Factory
class MoviesEntityMapper : BaseMapper<MovieEntity, Movie> {
    override fun map(input: MovieEntity): Movie =
        Movie(
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
