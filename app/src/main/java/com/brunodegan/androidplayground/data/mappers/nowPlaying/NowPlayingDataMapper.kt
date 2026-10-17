package com.brunodegan.androidplayground.data.mappers.nowPlaying

import com.brunodegan.androidplayground.base.utils.formatFullCDNUrl
import com.brunodegan.androidplayground.base.utils.formatUsDateToBrDate
import com.brunodegan.androidplayground.base.utils.orZero
import com.brunodegan.androidplayground.data.datasources.local.entities.MoviesApiDataResponse
import com.brunodegan.androidplayground.data.datasources.local.entities.NowPlayingMoviesEntity
import com.brunodegan.androidplayground.data.mappers.BaseMapper
import org.koin.core.annotation.Factory
import kotlin.math.roundToInt
import kotlin.random.Random

@Factory
class NowPlayingDataMapper : BaseMapper<MoviesApiDataResponse, List<NowPlayingMoviesEntity>> {
    override fun map(input: MoviesApiDataResponse): List<NowPlayingMoviesEntity> =
        buildList {
            input.results.forEach { movie ->
                add(
                    NowPlayingMoviesEntity(
                        id = movie.id ?: Random.nextInt(),
                        title = movie.title.orEmpty(),
                        posterPath = movie.posterPath.formatFullCDNUrl(),
                        overview = movie.overview.orEmpty(),
                        originalLanguage = movie.originalLanguage.orEmpty(),
                        popularity = movie.popularity.orZero(),
                        voteAverage = (movie.voteAverage.orZero() / 2).roundToInt(),
                        releaseDate = movie.releaseDate.formatUsDateToBrDate(),
                        isFavorite = false,
                    ),
                )
            }
        }
}
