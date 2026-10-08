# Data Model

Models live in `ApiEntity.kt`. Wire names stay identical; only the annotation mechanism changes (`@SerializedName` to `@Serializable` + `@SerialName`). `@Parcelize` is kept.

## MoviesApiDataResponse
- `results: List<Movies>` (JSON `results`)

## Movies
| Field | JSON | Type |
|-------|------|------|
| id | `id` | Int? |
| title | `title` | String? |
| posterPath | `poster_path` | String? |
| overview | `overview` | String? |
| originalLanguage | `original_language` | String? |
| popularity | `popularity` | Double? |
| releaseDate | `release_date` | String? |
| voteAverage | `vote_average` | Double? |

Nullable fields need `= null` defaults or `explicitNulls = false` so missing keys decode like Gson did.

## AddToFavoritesRequest
| Field | JSON | Type | Default |
|-------|------|------|---------|
| mediaType | `media_type` | String | `"movie"` (must be encoded: `encodeDefaults = true`) |
| mediaId | `media_id` | Int | none |
| favorite | `favorite` | Boolean | none |

## AddToFavoritesApiResponse
- `success: Boolean`, `status_code: Int`, `status_message: String`.
- Decision: TMDB returns `status_code` as a number; Gson coerced it to String but kotlinx is strict, so the field type changes from `String` to `Int`. `AddToFavoriteMoviesData.statusCode` stays `String` (mapper converts with `toString()`). Locked by a test with the real payload shape (tasks T017).

## Unchanged
`AddToFavoriteMoviesData`, `BaseApiData`/`ApiData` (domain/Parcelable only, not serialized).
