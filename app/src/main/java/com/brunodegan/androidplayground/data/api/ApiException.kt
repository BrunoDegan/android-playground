package com.brunodegan.androidplayground.data.api

class ApiException(
    val statusCode: Int,
    val body: String,
) : Exception("HTTP $statusCode: $body")
