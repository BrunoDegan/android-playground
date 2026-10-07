---

description: "Task list for migrating the networking layer from Retrofit to Ktor"
---

# Tasks: Migrate Networking Layer from Retrofit to Ktor

**Input**: Design documents from `/specs/001-retrofit-to-ktor/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/tmdb-endpoints.md, quickstart.md

**Tests**: Included. Spec FR-009 requires the API-layer tests to be migrated, and FR-003a requires error-path coverage.

**Organization**: Grouped by user story. Because the Retrofit-created `RestApiService` is swapped for a Ktor-backed one in a single DI provider, the client, models and all six endpoint calls land in Phase 2 (one compile unit). User story phases then prove each story with tests and verification; US3 removes the legacy stack.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1 = movie lists, US2 = favorites, US3 = legacy removal and logging

## Path Conventions

Single Android module. Roots used below:

- `MAIN` = `app/src/main/java/com/brunodegan/androidplayground`
- `TEST` = `app/src/test/java/com/brunodegan/androidplayground`
- `FIXT` = `app/src/testFixtures/java/com/brunodegan/androidplayground/testfixtures`

---

## Phase 1: Setup (dependencies)

- [X] T001 Add Ktor 3.5.2 entries to `gradle/libs.versions.toml`: version `ktor = "3.5.2"`; libraries `ktor-client-core`, `ktor-client-okhttp`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`, `ktor-client-logging`, `ktor-client-mock`; plus `kotlinx-serialization-json` (use the version compatible with Kotlin 2.4.20). Also add an explicit `gson` library entry (`com.google.code.gson:gson`): Gson is only transitive via `retrofit-gson-converter` today but is still needed by the Room `DatabaseTypeConverter` and must survive the converter removal. Keep all Retrofit entries for now.
- [X] T002 Add the new dependencies to `app/build.gradle.kts` (`implementation` for core, okhttp, content-negotiation, serialization-kotlinx-json, logging, kotlinx-serialization-json; `testImplementation` for `ktor-client-mock`) and `implementation(libs.gson)` (and `testFixturesImplementation(libs.gson)` for `MockUtils` if it still needs Gson). The `kotlinSerialization` plugin is already applied via `build-logic/src/main/kotlin/build.logic.gradle.kts`; no change needed. Run `./gradlew help` to confirm sync.

---

## Phase 2: Foundational (blocking)

**Purpose**: New client, models, error type, and DI wiring. No user story can be verified until this compiles.

- [X] T003 [P] Create `MAIN/data/api/ApiException.kt`: exception carrying `statusCode: Int` and `body: String`. Used for every non-2xx response (FR-003a).
- [X] T004 Update `MAIN/data/datasources/local/entities/ApiEntity.kt`: add `@Serializable` to `MoviesApiDataResponse`, `Movies`, `AddToFavoritesApiResponse`, `AddToFavoritesRequest`; replace each `@SerializedName("x")` with `@SerialName("x")` (same wire names, see data-model.md); keep `@Parcelize`. Give nullable `Movies` fields `= null` defaults. `AddToFavoritesRequest.mediaType` keeps default `MEDIA_TYPE` ("movie") and must still be encoded. Resolve the `status_code` typing risk: TMDB returns a number, so change `AddToFavoritesApiResponse.statusCode` from `String` to `Int` (strict decoding, no lenient mode). Update every usage: the mapper to `AddToFavoriteMoviesData` (`statusCode: String`, convert with `toString()` there unless the domain type is also changed), `MockUtils` mocks, and any tests.
- [X] T005 Remove Retrofit annotations and imports from `MAIN/data/api/RestApiService.kt`; keep the six `suspend` signatures, parameter names, defaults and all companion constants exactly as in contracts/tmdb-endpoints.md. Make the path constants `internal` so the implementation can use them.
- [X] T006 Create `MAIN/data/api/KtorRestApiService.kt` implementing `RestApiService` with an injected `HttpClient`. Implement all six calls with the paths, methods, `language` query and `account_id` path segment from contracts/tmdb-endpoints.md; POST sends JSON body with `contentType(ContentType.Application.Json)`. Add one private helper that executes the request, checks `response.status.isSuccess()`, throws `ApiException(status.value, bodyAsText())` for anything else (do NOT use `expectSuccess` or `HttpCallValidator`), then decodes with `body<T>()`.
- [X] T007 Rewrite `MAIN/base/network/NetworkModule.kt`: `@Singleton` provider returning `RestApiService` backed by `KtorRestApiService(HttpClient(OkHttp) { ... })`. Set `followRedirects = false` on the client config (OkHttp otherwise follows redirects, so a 3xx would never reach the status check and FR-003a would be violated in production). Install `ContentNegotiation` with `Json { ignoreUnknownKeys = true; encodeDefaults = true }` (add `explicitNulls = false`/`coerceInputValues` only if tests require), `HttpTimeout` (connect, socket and request = 60_000 ms), `Logging` (`LogLevel.BODY` when `BuildConfig.DEBUG`, `LogLevel.NONE` otherwise; sanitize the Authorization header), and `defaultRequest` with base URL `BASE_URL` plus headers `ACCEPT`, `CONTENT_TYPE`, `AUTHORIZATION_HEADER` (`BuildConfig.TMDB_BEARER_TOKEN`). Remove the Retrofit/OkHttp builders and Gson converter factory from this file; keep the `@Module @Configuration @ComponentScan` annotations.
- [X] T008 [P] Update `FIXT/MockUtils.kt`: change `toJsonString` (line ~194) from `Gson().toJson(obj)` to a kotlinx `Json.encodeToString` with `encodeDefaults = true` so snake_case `@SerialName` fields are produced. Remove the Gson import there if unused. Because `toJsonString(obj: Any)` cannot resolve a serializer for `Any`, change its signature to a reified generic (`inline fun <reified T> toJsonString(obj: T)`). First grep all callers (`git grep -n toJsonString`) and update each; the known caller is `RestApiServiceTest`.
- [X] T009 Run `./gradlew assembleDebug` and fix compile errors from T003-T008. Confirm no callers other than `NetworkModule` and tests referenced Retrofit types.

**Checkpoint**: App compiles; Retrofit still on classpath but unused by main code.

---

## Phase 3: User Story 1 - Movie lists keep working (Priority: P1) MVP

**Goal**: Now Playing, Popular, Top Rated, Upcoming return identical data, language and error behavior.

**Independent Test**: Run the new API tests for the four list endpoints, then smoke the four lists in the app (quickstart.md section 3).

- [X] T010 [US1] Rewrite `TEST/data/datasources/data/api/RestApiServiceTest.kt` setup to build `KtorRestApiService` over a `MockEngine`-based `HttpClient` (same `ContentNegotiation`/`Json` config as production, same default headers, base URL `https://api.themoviedb.org/3/`). Remove all Retrofit/MockWebServer imports. Keep existing GIVEN/WHEN/THEN naming.
- [X] T011 [US1] In the same test file, add or migrate tests for `fetchNowPlaying`, `fetchPopular`, `fetchTopRated`, `fetchUpcoming`: assert HTTP method GET, URL path (`movie/now_playing`, `movie/popular`, `movie/top_rated`, `movie/upcoming`), query `language=pt-BR` by default and when overridden, headers Accept/Content-Type/Authorization present, and decoded result equals `MockUtils.mockMoviesApiDataResponse()`.
- [X] T012 [US1] In the same test file, add error-path tests for a list endpoint: status 401, 404, 500 and 302 each throw `ApiException` with the matching `statusCode` and non-empty `body`; 200 with a body missing optional fields still decodes.
- [X] T013 [US1] Run `./gradlew testDebugUnitTest --tests "*RestApiServiceTest" --tests "*RemoteDataSourceImplTest"`; both must pass without editing `RemoteDataSourceImplTest`.
- [X] T014 [US1] `MAIN/data/repositories/MoviesRepositoryImpl.kt` wraps every remote call in `runCatching` and has no `HttpException` handling, so `ApiException` is already caught. Verify that by adding or confirming a repository test where the remote data source throws `ApiException` and the failure path is taken; no production change expected. (Pre-existing: `runCatching` also swallows `CancellationException`; out of scope.)
- [ ] T015 [US1] Manual smoke per quickstart.md steps 3.1-3.2 and 3.4 on a debug build (lists render in pt-BR, airplane mode gives the same error behavior, no crash).

**Checkpoint**: US1 verified and shippable on its own.

---

## Phase 4: User Story 2 - Favorites keep working (Priority: P1)

**Goal**: Adding to favorites and listing favorites behave as before for the configured account.

**Independent Test**: Run the favorites API tests, then favorite a movie and open Favorites in the app.

- [X] T016 [US2] In `TEST/data/datasources/data/api/RestApiServiceTest.kt`, add or migrate a test for `addToFavorites`: method POST, path `account/<BuildConfig.TMDB_ACCOUNT_ID>/favorite` (and an explicit `accountId` override), request body JSON equals `{"media_type":"movie","media_id":<id>,"favorite":<bool>}` (proves `encodeDefaults`), Content-Type JSON, decoded result equals the mocked `AddToFavoritesApiResponse`.
- [X] T017 [US2] Add a decode test for `addToFavorites` using a realistic TMDB payload (numeric `status_code`, e.g. `{"success":true,"status_code":1,"status_message":"Success."}`) asserting `statusCode == 1` (`Int`, per T004).
- [X] T018 [US2] Add tests for `getFavorites`: GET, path `account/<id>/favorite/movies`, no body, decoded result equals the mock; plus a non-2xx case (`ApiException`) for both `addToFavorites` and `getFavorites`.
- [X] T019 [US2] Run `./gradlew testDebugUnitTest`; full unit suite green.
- [ ] T020 [US2] Manual smoke per quickstart.md step 3.3 (favorite a movie, open Favorites, it appears).

**Checkpoint**: US1 and US2 both verified.

---

## Phase 5: User Story 3 - Single HTTP stack, debug-only logging (Priority: P2)

**Goal**: No legacy HTTP client left; logging only in debug; release build safe.

**Independent Test**: Legacy grep returns nothing; debug logs bodies; release does not and runs.

- [X] T021 [US3] Remove from `app/build.gradle.kts`: `libs.retrofit.core`, `libs.retrofit.kotlin.serialization`, `libs.retrofit.gson.converter` (including the `testFixturesImplementation` line), `libs.okhttp.logging`, `libs.okhttp.mockwebserver`. Keep `libs.gson` (added in T002) for the Room `DatabaseTypeConverter`.
- [X] T022 [US3] Remove the now-unused entries from `gradle/libs.versions.toml`: `retrofit` version, `retrofit-core`, `retrofit-gson-converter`, `retrofit-kotlin-serialization`, `okhttp-logging`, `okhttp-mockwebserver`; keep the `okhttp` version only if still referenced; the `gson` entry from T001 stays.
- [X] T023 [US3] Run `git grep -inE "retrofit|mockwebserver|okhttp3.logging|GsonConverterFactory"` and remove any remaining references in source, build files, ProGuard/R8 rules and docs.
- [X] T024 [US3] Check R8: run `./gradlew assembleRelease`; add keep rules for kotlinx.serialization only if the build or the release smoke shows missing-class/serializer errors (the serialization plugin ships consumer rules, so none are expected).
- [ ] T025 [US3] Verify logging: debug build logs request/response bodies in Logcat; release build logs nothing and the Authorization header is not exposed in debug logs (quickstart.md steps 3.5 and 4).

**Checkpoint**: One HTTP stack; all stories verified.

---

## Phase 6: Polish and cross-cutting

- [X] T026 [P] Run `./gradlew ktlintCheck lintDebug` and fix findings.
- [ ] T027 Run the full quickstart.md end to end, including the data comparison against `main` (section 5) and the release smoke (section 4).
- [X] T028 [P] Confirm with a code check that the production client config contains `followRedirects = false` (set in T007) and that `HttpTimeout` uses 60_000 ms for connect, socket and request (FR-004).
- [X] T029 Update docs mentioning Retrofit (README or `docs/`), if any, to describe the Ktor client.

---

## Dependencies and Execution Order

- Phase 1 then Phase 2 (blocks everything). T003 and T008 can run in parallel with each other; T004-T007 are sequential (same compile unit); T009 last.
- Phase 3 (US1) after Phase 2. Phase 4 (US2) after Phase 2; it edits the same test file as US1, so run after US1 (or merge carefully).
- Phase 5 (US3) after US1 and US2 are green (it deletes the fallback dependencies).
- Phase 6 last.

## Parallel Opportunities

- T003 and T008 (different files, no dependency).
- T026 and T028 and T029 in the polish phase.
- Manual smokes (T015, T020) can run in one session once both stories pass.

## Implementation Strategy

- **MVP**: Phases 1-3. The app is on Ktor with the four list endpoints verified; Retrofit is only unused ballast until Phase 5.
- **Incremental**: add US2 verification, then remove legacy (US3), then polish.
- Commit after each phase; keep the build green at every checkpoint.
