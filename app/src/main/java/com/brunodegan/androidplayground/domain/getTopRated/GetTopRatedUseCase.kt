package com.brunodegan.androidplayground.domain.getTopRated

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface GetTopRatedUseCase {
    suspend operator fun invoke(): Flow<Resource<ImmutableList<Movie>>>
}
