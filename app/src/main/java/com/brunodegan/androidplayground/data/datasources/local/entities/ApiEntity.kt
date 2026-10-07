package com.brunodegan.androidplayground.data.datasources.local.entities

import android.os.Parcelable
import com.brunodegan.androidplayground.data.api.KtorRestApiService.Companion.MEDIA_TYPE
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class MoviesApiDataResponse(
    @SerialName("results") val results: List<Movies>,
) : ApiData()

@Parcelize
@Serializable
data class Movies(
    @SerialName("id") val id: Int? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("popularity") val popularity: Double? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
) : ApiData()

@Parcelize
@Serializable
data class AddToFavoritesApiResponse(
    @SerialName("success") val success: Boolean,
    // TMDB sends status_code as a JSON number (e.g. 1, 12, 13)
    @SerialName("status_code") val statusCode: Int,
    @SerialName("status_message") val statusMessage: String,
) : ApiData()

@Parcelize
@Serializable
data class AddToFavoritesRequest(
    @SerialName("media_type") val mediaType: String = MEDIA_TYPE,
    @SerialName("media_id") val mediaId: Int,
    @SerialName("favorite") val favorite: Boolean,
) : ApiData()

@Parcelize
data class AddToFavoriteMoviesData(
    val success: Boolean,
    val statusMessage: String,
    val statusCode: String,
) : ApiData()

@Parcelize
open class BaseApiData : Parcelable

typealias ApiData = BaseApiData
