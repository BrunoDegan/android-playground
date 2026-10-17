package com.brunodegan.androidplayground.domain.getTopRated

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.domain.model.Movie
import com.brunodegan.androidplayground.domain.repositories.MoviesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetTopRatedUseCaseImpl(
    private val repository: MoviesRepository,
) : GetTopRatedUseCase {
    override suspend fun invoke(): Flow<Resource<ImmutableList<Movie>>> = repository.getTopRateMovies()
}
