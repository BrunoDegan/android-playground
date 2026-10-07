package com.brunodegan.androidplayground.base.network

import com.brunodegan.androidplayground.BuildConfig
import com.brunodegan.androidplayground.data.api.KtorRestApiService
import com.brunodegan.androidplayground.data.api.KtorRestApiService.Companion.ACCEPT
import com.brunodegan.androidplayground.data.api.KtorRestApiService.Companion.APPLICATION_JSON
import com.brunodegan.androidplayground.data.api.KtorRestApiService.Companion.AUTHORIZATION_HEADER
import com.brunodegan.androidplayground.data.api.KtorRestApiService.Companion.BASE_URL
import com.brunodegan.androidplayground.data.api.KtorRestApiService.Companion.CONTENT_TYPE
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("com.brunodegan.androidplayground.base.network")
class NetworkModule {
    @Singleton
    fun provideRestClient(): KtorRestApiService {
        val okHttpclient = OkHttp.create()
        return KtorRestApiService(createHttpClient(okHttpclient))
    }

    companion object {
        private const val REQUEST_TIMEOUT_MS = 60_000L

        // Engine is a parameter so tests exercise the exact production configuration with a MockEngine
        internal fun createHttpClient(engine: HttpClientEngine): HttpClient =
            HttpClient(engine) {
                followRedirects = false

                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            encodeDefaults = true
                        },
                    )
                }

                install(HttpTimeout) {
                    connectTimeoutMillis = REQUEST_TIMEOUT_MS
                    socketTimeoutMillis = REQUEST_TIMEOUT_MS
                    requestTimeoutMillis = REQUEST_TIMEOUT_MS
                }

                install(Logging) {
                    logger = Logger.ANDROID
                    level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
                    sanitizeHeader { it == HttpHeaders.Authorization || it == HttpHeaders.Cookie || it == HttpHeaders.AuthenticationInfo }
                }

                defaultRequest {
                    url(BASE_URL)
                    header(ACCEPT, APPLICATION_JSON)
                    header(CONTENT_TYPE, APPLICATION_JSON)
                    header(AUTHORIZATION_HEADER, BuildConfig.TMDB_BEARER_TOKEN)
                }
            }
    }
}
