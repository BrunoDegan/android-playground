package com.brunodegan.androidplayground.base.utils

import java.text.SimpleDateFormat
import java.util.Locale

private const val MOVIES_POSTER_CDN_URL = "https://image.tmdb.org/t/p/original"

fun String?.formatFullCDNUrl(): String {
    if (this.isNullOrEmpty()) return ""
    return "$MOVIES_POSTER_CDN_URL$this"
}

fun String?.formatUsDateToBrDate(): String {
    if (this.isNullOrEmpty()) return ""
    return try {
        val usDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val brDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        brDateFormat.format(usDateFormat.parse(this) ?: return "")
    } catch (e: Exception) {
        ""
    }
}

fun String.toSortableDate(): String = split("/").reversed().joinToString("")

private const val DEFAULT_CACHE_EXPIRATION_MILLIS = 10 * 60 * 1000L

fun isCacheValid(
    lastUpdated: Long,
    expirationTime: Long = DEFAULT_CACHE_EXPIRATION_MILLIS,
): Boolean = System.currentTimeMillis() - lastUpdated < expirationTime

fun Double?.orZero(): Double = this ?: 0.0
