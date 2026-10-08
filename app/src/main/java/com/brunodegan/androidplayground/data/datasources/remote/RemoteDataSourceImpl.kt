package com.brunodegan.androidplayground.data.datasources.remote

import com.brunodegan.androidplayground.data.api.KtorRestApiService
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesRequest
import org.koin.core.annotation.Single

@Single
class RemoteDataSourceImpl(
    private val restApi: KtorRestApiService,
) : RemoteDataSource {
    override suspend fun fetchNowPlaying() = restApi.fetchNowPlaying()

    override suspend fun fetchPopular() = restApi.fetchPopular()

    override suspend fun fetchTopRated() = restApi.fetchTopRated()

    override suspend fun fetchUpcoming() = restApi.fetchUpcoming()

    override suspend fun addOrRemoveFromFavorites(requestData: AddToFavoritesRequest) =
        restApi.addToFavorites(addToFavoritesRequest = requestData)

    override suspend fun fetchFavorites() = restApi.getFavorites()
}
