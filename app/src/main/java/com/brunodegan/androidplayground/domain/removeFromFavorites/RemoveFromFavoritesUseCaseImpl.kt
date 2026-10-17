package com.brunodegan.androidplayground.domain.removeFromFavorites

import com.brunodegan.androidplayground.base.network.base.Resource
import com.brunodegan.androidplayground.domain.model.FavoriteResult
import com.brunodegan.androidplayground.domain.repositories.MoviesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class RemoveFromFavoritesUseCaseImpl(
    private val repository: MoviesRepository,
) : RemoveFromFavoritesUseCase {
    override suspend fun invoke(id: Int): Flow<Resource<FavoriteResult>> = repository.removeFavorite(id)
}
