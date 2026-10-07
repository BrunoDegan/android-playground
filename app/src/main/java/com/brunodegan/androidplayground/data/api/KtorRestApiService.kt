package com.brunodegan.androidplayground.data.api

import com.brunodegan.androidplayground.data.api.RestApiService.Companion.LANGUAGE
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.NOW_PLAYING_URL
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.POPULAR_URL
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.PT_BR
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.TOP_RATED_URL
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.UPCOMING_URL
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.addToFavoritesUrl
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.favoritesUrl
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
) : RestApiService {
    override suspend fun fetchNowPlaying(): MoviesApiDataResponse = getMovies(NOW_PLAYING_URL)

    override suspend fun fetchPopular(): MoviesApiDataResponse = getMovies(POPULAR_URL)

    override suspend fun fetchTopRated(): MoviesApiDataResponse = getMovies(TOP_RATED_URL)

    override suspend fun fetchUpcoming(): MoviesApiDataResponse = getMovies(UPCOMING_URL)

    override suspend fun addToFavorites(
        accountId: String,
        addToFavoritesRequest: AddToFavoritesRequest,
    ): AddToFavoritesApiResponse =
        client
            .post(addToFavoritesUrl(accountId)) {
                contentType(ContentType.Application.Json)
                setBody(addToFavoritesRequest)
            }.decode()

    override suspend fun getFavorites(accountId: String): MoviesApiDataResponse = client.get(favoritesUrl(accountId)).decode()

    private suspend fun getMovies(path: String): MoviesApiDataResponse = client.get(path) { parameter(LANGUAGE, PT_BR) }.decode()

    private suspend inline fun <reified T> HttpResponse.decode(): T {
        if (!status.isSuccess()) throw ApiException(status.value, bodyAsText())
        return body()
    }
}
