# Contract: TMDB endpoints (must stay identical)

Base URL: `https://api.themoviedb.org/3/`

Headers on every request: `accept: application/json`, `content-type: application/json`, `Authorization: <BuildConfig.TMDB_BEARER_TOKEN>`.

| Operation | Method | Path | Query / body | Response |
|-----------|--------|------|--------------|----------|
| fetchNowPlaying | GET | `movie/now_playing` | `language=pt-BR` (fixed, not a caller parameter) | MoviesApiDataResponse |
| fetchPopular | GET | `movie/popular` | `language=pt-BR` (fixed, not a caller parameter) | MoviesApiDataResponse |
| fetchTopRated | GET | `movie/top_rated` | `language=pt-BR` (fixed, not a caller parameter) | MoviesApiDataResponse |
| fetchUpcoming | GET | `movie/upcoming` | `language=pt-BR` (fixed, not a caller parameter) | MoviesApiDataResponse |
| addToFavorites | POST | `account/{account_id}/favorite` | JSON body AddToFavoritesRequest; `account_id` default `BuildConfig.TMDB_ACCOUNT_ID` | AddToFavoritesApiResponse |
| getFavorites | GET | `account/{account_id}/favorite/movies` | `account_id` default `BuildConfig.TMDB_ACCOUNT_ID` | MoviesApiDataResponse |

## Kotlin contract (unchanged)

`KtorRestApiService` (the former `RestApiService` interface was removed) keeps the same six `suspend` functions, parameter names (except the removed `language` parameter on the four list calls), defaults and return types, plus the companion constants (`BASE_URL`, header names, `MEDIA_TYPE`). Only Retrofit annotations are removed. `getFavorites` and the four list calls send no body.
