package com.brunodegan.androidplayground.data.datasources.data.api

import com.brunodegan.androidplayground.BuildConfig
import com.brunodegan.androidplayground.base.network.NetworkModule
import com.brunodegan.androidplayground.data.api.ApiException
import com.brunodegan.androidplayground.data.api.KtorRestApiService
import com.brunodegan.androidplayground.data.api.RestApiService
import com.brunodegan.androidplayground.testfixtures.MockUtils
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RestApiServiceTest {
    private val requests = mutableListOf<HttpRequestData>()
    private var status = HttpStatusCode.OK
    private var responseBody = ""
    private lateinit var apiService: RestApiService

    @Before
    fun setup() {
        val engine =
            MockEngine { request ->
                requests += request
                respond(
                    content = responseBody,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        apiService = KtorRestApiService(NetworkModule.createHttpClient(engine))
    }

    @Test
    fun `GIVEN mock response WHEN fetchNowPlaying is called THEN verify response`() =
        runBlocking {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchNowPlaying()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/now_playing", language = "pt-BR")
        }

    @Test
    fun `GIVEN mock response WHEN fetchTopRated is called THEN verify response`() =
        runBlocking {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchTopRated()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/top_rated", language = "pt-BR")
        }

    @Test
    fun `GIVEN mock response WHEN fetchPopular is called THEN verify response`() =
        runBlocking {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchPopular()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/popular", language = "pt-BR")
        }

    @Test
    fun `GIVEN mock response WHEN fetchUpcoming is called THEN verify response`() =
        runBlocking {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchUpcoming()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/upcoming", language = "pt-BR")
        }

    @Test
    fun `GIVEN custom language WHEN fetchNowPlaying is called THEN language query is overridden`() =
        runBlocking {
            responseBody = MockUtils.toJsonString(MockUtils.mockMoviesApiDataResponse())

            apiService.fetchNowPlaying(language = "en-US")

            assertRequest(HttpMethod.Get, "/3/movie/now_playing", language = "en-US")
        }

    @Test
    fun `GIVEN payload with missing optional fields and unknown keys WHEN fetchPopular is called THEN it decodes`() =
        runBlocking {
            responseBody = """{"page":1,"results":[{"id":7,"title":"Only Title","unknown":true}]}"""

            val result = apiService.fetchPopular()

            assertEquals(1, result.results.size)
            assertEquals(7, result.results.first().id)
            assertEquals("Only Title", result.results.first().title)
            assertEquals(null, result.results.first().posterPath)
        }

    @Test
    fun `GIVEN error status WHEN fetchNowPlaying is called THEN throws ApiException with status code and body`() {
        listOf(
            HttpStatusCode.Unauthorized,
            HttpStatusCode.NotFound,
            HttpStatusCode.InternalServerError,
            HttpStatusCode.Found,
        ).forEach { errorStatus ->
            status = errorStatus
            responseBody = """{"status_message":"boom"}"""

            val exception = assertThrows(ApiException::class.java) { runBlocking { apiService.fetchNowPlaying() } }

            assertEquals(errorStatus.value, exception.statusCode)
            assertEquals("""{"status_message":"boom"}""", exception.body)
        }
    }

    @Test
    fun `GIVEN mock response WHEN addToFavorites is called THEN verify response and request`() =
        runBlocking {
            val mockResponse = MockUtils.mockAddToFavoritesApiResponse()
            val mockRequest = MockUtils.mockAddToFavoritesRequest()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.addToFavorites(addToFavoritesRequest = mockRequest)

            assertEquals(mockResponse, result)
            val request = requests.single()
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/3/account/${BuildConfig.TMDB_ACCOUNT_ID}/favorite", request.url.encodedPath)
            assertEquals(
                """{"media_type":"movie","media_id":1,"favorite":true}""",
                (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString(),
            )
            assertTrue(
                request.body.contentType
                    .toString()
                    .startsWith("application/json"),
            )
        }

    @Test
    fun `GIVEN explicit account id WHEN addToFavorites is called THEN path uses it`() =
        runBlocking {
            responseBody = MockUtils.toJsonString(MockUtils.mockAddToFavoritesApiResponse())

            apiService.addToFavorites(accountId = "42", addToFavoritesRequest = MockUtils.mockAddToFavoritesRequest())

            assertEquals("/3/account/42/favorite", requests.single().url.encodedPath)
        }

    @Test
    fun `GIVEN TMDB numeric status_code payload WHEN addToFavorites is called THEN status code decodes as Int`() =
        runBlocking {
            responseBody = """{"success":true,"status_code":1,"status_message":"Success."}"""

            val result = apiService.addToFavorites(addToFavoritesRequest = MockUtils.mockAddToFavoritesRequest())

            assertEquals(1, result.statusCode)
            assertEquals("Success.", result.statusMessage)
        }

    @Test
    fun `GIVEN mock response WHEN getFavorites is called THEN verify response`() =
        runBlocking {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.getFavorites()

            assertEquals(mockResponse, result)
            val request = requests.single()
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/3/account/${BuildConfig.TMDB_ACCOUNT_ID}/favorite/movies", request.url.encodedPath)
        }

    @Test
    fun `GIVEN error status WHEN favorites calls are made THEN each throws ApiException`() {
        status = HttpStatusCode.InternalServerError
        responseBody = "error"

        val getException = assertThrows(ApiException::class.java) { runBlocking { apiService.getFavorites() } }
        val addException =
            assertThrows(ApiException::class.java) {
                runBlocking { apiService.addToFavorites(addToFavoritesRequest = MockUtils.mockAddToFavoritesRequest()) }
            }

        assertEquals(500, getException.statusCode)
        assertEquals(500, addException.statusCode)
    }

    private fun assertRequest(
        method: HttpMethod,
        path: String,
        language: String,
    ) {
        val request = requests.single()
        assertEquals(method, request.method)
        assertEquals("api.themoviedb.org", request.url.host)
        assertEquals(path, request.url.encodedPath)
        assertEquals(language, request.url.parameters["language"])
        assertEquals("application/json", request.headers[RestApiService.ACCEPT])
        assertEquals("application/json", request.headers[RestApiService.CONTENT_TYPE])
        assertEquals(BuildConfig.TMDB_BEARER_TOKEN, request.headers[RestApiService.AUTHORIZATION_HEADER])
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
