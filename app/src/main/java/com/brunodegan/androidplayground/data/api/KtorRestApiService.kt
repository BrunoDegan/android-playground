package com.brunodegan.androidplayground.data.api

import com.brunodegan.androidplayground.BuildConfig
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesApiResponse
import com.brunodegan.androidplayground.data.datasources.local.entities.AddToFavoritesRequest
import com.brunodegan.androidplayground.data.datasources.local.entities.MoviesApiDataResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class KtorRestApiService(
    private val client: HttpClient,
) {
    suspend fun fetchNowPlaying(): MoviesApiDataResponse = getMovies(NOW_PLAYING_URL)

    suspend fun fetchPopular(): MoviesApiDataResponse = getMovies(POPULAR_URL)

    suspend fun fetchTopRated(): MoviesApiDataResponse = getMovies(TOP_RATED_URL)

    suspend fun fetchUpcoming(): MoviesApiDataResponse = getMovies(UPCOMING_URL)

    suspend fun addToFavorites(
        accountId: String = BuildConfig.TMDB_ACCOUNT_ID,
        addToFavoritesRequest: AddToFavoritesRequest,
    ): AddToFavoritesApiResponse =
        client
            .post(addToFavoritesUrl(accountId)) {
                contentType(ContentType.Application.Json)
                setBody(addToFavoritesRequest)
            }.decode()

    suspend fun getFavorites(accountId: String = BuildConfig.TMDB_ACCOUNT_ID): MoviesApiDataResponse =
        client.get(favoritesUrl(accountId)).decode()

    private suspend fun getMovies(path: String): MoviesApiDataResponse = client.get(path) { parameter(LANGUAGE, PT_BR) }.decode()

    private suspend inline fun <reified T> HttpResponse.decode(): T {
        if (!status.isSuccess()) throw ApiException(status.value, bodyAsText())
        return body()
    }

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val APPLICATION_JSON = "application/json"
        const val CONTENT_TYPE = "content-type"
        const val ACCEPT = "accept"
        const val BASE_URL = "https://api.themoviedb.org/3/"

        internal const val MEDIA_TYPE = "movie"
        private const val NOW_PLAYING_URL = "movie/now_playing"
        private const val POPULAR_URL = "movie/popular"
        private const val TOP_RATED_URL = "movie/top_rated"
        private const val UPCOMING_URL = "movie/upcoming"
        private const val LANGUAGE = "language"
        private const val PT_BR = "pt-BR"

        private fun addToFavoritesUrl(accountId: String) = "account/$accountId/favorite"

        private fun favoritesUrl(accountId: String) = "account/$accountId/favorite/movies"
    }
}
