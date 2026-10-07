package com.brunodegan.androidplayground.data.datasources.local.entities

import android.os.Parcelable
import com.brunodegan.androidplayground.data.api.RestApiService.Companion.MEDIA_TYPE
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Parcelize
data class MoviesApiDataResponse(
    @SerialName("results") val results: List<Movies>,
) : ApiData()

@Parcelize
data class Movies(
    @SerialName("id") val id: Int?,
    @SerialName("title") val title: String?,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("overview") val overview: String?,
    @SerialName("original_language") val originalLanguage: String?,
    @SerialName("popularity") val popularity: Double?,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("vote_average") val voteAverage: Double?,
) : ApiData()

@Parcelize
data class AddToFavoritesApiResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("status_code") val statusCode: String,
    @SerialName("status_message") val statusMessage: String,
) : ApiData()

@Parcelize
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
