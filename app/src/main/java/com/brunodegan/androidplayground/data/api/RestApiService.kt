package com.brunodegan.androidplayground.data.api

import com.brunodegan.androidplayground.BuildConfig
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesApiResponse
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesRequest
import com.brunodegan.androidplayground.data.datasources.local.entities.MoviesApiDataResponse

interface RestApiService {
    suspend fun fetchNowPlaying(language: String = PT_BR): MoviesApiDataResponse

    suspend fun fetchPopular(language: String = PT_BR): MoviesApiDataResponse

    suspend fun fetchTopRated(language: String = PT_BR): MoviesApiDataResponse

    suspend fun fetchUpcoming(language: String = PT_BR): MoviesApiDataResponse

    suspend fun addToFavorites(
        accountId: String = BuildConfig.TMDB_ACCOUNT_ID,
        addToFavoritesRequest: AddToFavoritesRequest,
    ): AddToFavoritesApiResponse

    suspend fun getFavorites(accountId: String = BuildConfig.TMDB_ACCOUNT_ID): MoviesApiDataResponse

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val APPLICATION_JSON = "application/json"
        const val CONTENT_TYPE = "content-type"
        const val ACCEPT = "accept"

        internal const val MEDIA_TYPE = "movie"
        internal const val NOW_PLAYING_URL = "movie/now_playing"
        internal const val POPULAR_URL = "movie/popular"
        internal const val TOP_RATED_URL = "movie/top_rated"
        internal const val UPCOMING_URL = "movie/upcoming"
        internal const val LANGUAGE = "language"
        internal const val PT_BR = "pt-BR"

        const val BASE_URL = "https://api.themoviedb.org/3/"

        internal fun addToFavoritesUrl(accountId: String) = "account/$accountId/favorite"

        internal fun favoritesUrl(accountId: String) = "account/$accountId/favorite/movies"
    }
}
