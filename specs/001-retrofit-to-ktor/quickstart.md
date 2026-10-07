# Quickstart: Validate the migration

## Prerequisites

- `local.properties` / build config provides `TMDB_BEARER_TOKEN` and `TMDB_ACCOUNT_ID`.
- Device or emulator with network access.

## 1. Automated checks

```powershell
./gradlew testDebugUnitTest
./gradlew lintDebug ktlintCheck
./gradlew assembleRelease
```

Expect: all green. API tests cover every endpoint in [contracts/tmdb-endpoints.md](contracts/tmdb-endpoints.md) (URL, method, query, headers, body) plus one non-2xx case.

## 2. Legacy removal

```powershell
git grep -inE "retrofit|mockwebserver|okhttp3.logging|GsonConverterFactory"
```

Expect: no matches in source or build files (Gson itself may remain for Room, see [research.md](research.md) R8).

## 3. Manual smoke (debug build)

1. Install and launch the app.
2. Open Now Playing, Popular, Top Rated, Upcoming: items render, text is pt-BR.
3. Favorite a movie, open Favorites: it appears.
4. Enable airplane mode and reopen a list: same error behavior as before migration, no crash.
5. Logcat shows request/response bodies in debug.

## 4. Release smoke

Install the shrunk release build, repeat step 3.2. Expect lists load; no serialization or missing-class crash; no body logging.

## 5. Data check

Compare list output with the pre-migration build (checkout `main`) for the same account. Models: [data-model.md](data-model.md).
