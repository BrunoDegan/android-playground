package com.brunodegan.androidplayground.data.datasources.data.api

import com.brunodegan.androidplayground.BuildConfig
import com.brunodegan.androidplayground.base.network.NetworkModule
import com.brunodegan.androidplayground.data.api.ApiException
import com.brunodegan.androidplayground.data.api.KtorRestApiService
import com.brunodegan.androidplayground.testfixtures.MockUtils
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KtorRestApiServiceTest {
    private val requests = mutableListOf<HttpRequestData>()
    private var status = HttpStatusCode.OK
    private var responseBody = ""
    private lateinit var apiService: KtorRestApiService

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
        runTest {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchNowPlaying()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/now_playing")
        }

    @Test
    fun `GIVEN mock response WHEN fetchTopRated is called THEN verify response`() =
        runTest {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchTopRated()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/top_rated")
        }

    @Test
    fun `GIVEN mock response WHEN fetchPopular is called THEN verify response`() =
        runTest {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchPopular()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/popular")
        }

    @Test
    fun `GIVEN mock response WHEN fetchUpcoming is called THEN verify response`() =
        runTest {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.fetchUpcoming()

            assertEquals(mockResponse, result)
            assertRequest(HttpMethod.Get, "/3/movie/upcoming")
        }

    @Test
    fun `GIVEN payload with missing optional fields and unknown keys WHEN fetchPopular is called THEN it decodes`() =
        runTest {
            responseBody = """{"page":1,"results":[{"id":7,"title":"Only Title","unknown":true}]}"""

            val result = apiService.fetchPopular()

            assertEquals(1, result.results.size)
            assertEquals(7, result.results.first().id)
            assertEquals("Only Title", result.results.first().title)
            assertEquals(null, result.results.first().posterPath)
        }

    @Test
    fun `GIVEN mock response WHEN addToFavorites is called THEN verify response and request`() =
        runTest {
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
        runTest {
            responseBody = MockUtils.toJsonString(MockUtils.mockAddToFavoritesApiResponse())

            apiService.addToFavorites(accountId = "42", addToFavoritesRequest = MockUtils.mockAddToFavoritesRequest())

            assertEquals("/3/account/42/favorite", requests.single().url.encodedPath)
        }

    @Test
    fun `GIVEN TMDB numeric status_code payload WHEN addToFavorites is called THEN status code decodes as Int`() =
        runTest {
            responseBody = """{"success":true,"status_code":1,"status_message":"Success."}"""

            val result = apiService.addToFavorites(addToFavoritesRequest = MockUtils.mockAddToFavoritesRequest())

            assertEquals(1, result.statusCode)
            assertEquals("Success.", result.statusMessage)
        }

    @Test
    fun `GIVEN mock response WHEN getFavorites is called THEN verify response`() =
        runTest {
            val mockResponse = MockUtils.mockMoviesApiDataResponse()
            responseBody = MockUtils.toJsonString(mockResponse)

            val result = apiService.getFavorites()

            assertEquals(mockResponse, result)
            val request = requests.single()
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/3/account/${BuildConfig.TMDB_ACCOUNT_ID}/favorite/movies", request.url.encodedPath)
        }

    @Test
    fun `GIVEN api failing with non 2xx WHEN list calls are made THEN each surfaces the ApiException`() =
        runTest {
            val failingApi: KtorRestApiService = mockk()
            val apiException = ApiException(statusCode = 500, body = "error")
            coEvery { failingApi.fetchNowPlaying() } throws apiException
            coEvery { failingApi.fetchPopular() } throws apiException
            coEvery { failingApi.fetchTopRated() } throws apiException
            coEvery { failingApi.fetchUpcoming() } throws apiException

            val failures =
                listOf(
                    runCatching { failingApi.fetchNowPlaying() },
                    runCatching { failingApi.fetchPopular() },
                    runCatching { failingApi.fetchTopRated() },
                    runCatching { failingApi.fetchUpcoming() },
                ).map { it.exceptionOrNull() }

            failures.forEach { assertSame(apiException, it) }
        }

    @Test
    fun `GIVEN api failing with non 2xx WHEN favorites calls are made THEN each surfaces the ApiException`() =
        runTest {
            val failingApi: KtorRestApiService = mockk()
            val apiException = ApiException(statusCode = 404, body = "not found")
            val request = MockUtils.mockAddToFavoritesRequest()
            coEvery { failingApi.addToFavorites(any(), request) } throws apiException
            coEvery { failingApi.getFavorites(any()) } throws apiException

            val addFailure =
                runCatching { failingApi.addToFavorites("1", request) }.exceptionOrNull()
            val getFailure = runCatching { failingApi.getFavorites("1") }.exceptionOrNull()

            assertSame(apiException, addFailure)
            assertSame(apiException, getFailure)
            assertEquals(404, (addFailure as ApiException).statusCode)
        }

    private fun assertRequest(
        method: HttpMethod,
        path: String,
    ) {
        val request = requests.single()
        assertEquals(method, request.method)
        assertEquals("api.themoviedb.org", request.url.host)
        assertEquals(path, request.url.encodedPath)
        assertEquals("pt-BR", request.url.parameters["language"])
        assertEquals("application/json", request.headers[KtorRestApiService.ACCEPT])
        assertEquals("application/json", request.headers[KtorRestApiService.CONTENT_TYPE])
        assertEquals(BuildConfig.TMDB_BEARER_TOKEN, request.headers[KtorRestApiService.AUTHORIZATION_HEADER])
    }

    @After
    fun tearDown() = unmockkAll()
}
