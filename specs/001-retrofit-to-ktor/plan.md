# Implementation Plan: Migrate Networking Layer from Retrofit to Ktor

**Branch**: `feature/moving-from-retrofit-to-ktorfit` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-retrofit-to-ktor/spec.md`

## Summary

Replace Retrofit + OkHttp-interceptor wiring with a Ktor `HttpClient` (OkHttp engine) behind the existing `RestApiService` interface. JSON moves from Gson to kotlinx.serialization (plugin already in the version catalog). Ktorfit is NOT adopted (see [research.md](research.md)). Callers (`RemoteDataSourceImpl`, repositories, view models) stay untouched because the interface is preserved; a new `KtorRestApiService` implements it.

## Technical Context

**Language/Version**: Kotlin 2.4.20

**Primary Dependencies**: Ktor client 3.5.2 (`ktor-client-core`, `-okhttp`, `-content-negotiation`, `-serialization-kotlinx-json`, `-logging`), kotlinx-serialization-json, Koin 4.2.2 + Koin compiler plugin 1.2.1 (existing)

**Storage**: N/A (Room untouched; its Gson `TypeConverter` is out of scope)

**Testing**: JUnit4, MockK, coroutines-test; `ktor-client-mock` (`MockEngine`) replaces `MockWebServer`

**Target Platform**: Android (single `app` module, build-logic convention plugin)

**Project Type**: mobile-app

**Performance Goals**: Parity with current behavior; no regression in list load time

**Constraints**: 60 s connect/read timeouts; Accept/Content-Type/Authorization headers on all requests; body logging debug-only; release shrinking must not break serialization

**Scale/Scope**: 6 endpoints, 4 response/request models, 1 DI module, 2 test files + 1 test fixture

## Constitution Check

`.specify/memory/constitution.md` is still the unfilled template: no ratified principles, so no gates apply. Plan follows repo conventions instead (Koin annotations, test naming `GIVEN/WHEN/THEN`, ktlint). Recommend running `/speckit-constitution` later. Re-check post-design: no violations; no new modules, no new abstractions beyond one implementation class.

## Project Structure

### Documentation (this feature)

```text
specs/001-retrofit-to-ktor/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── tmdb-endpoints.md
└── tasks.md             # /speckit-tasks
```

### Source Code (repository root)

```text
gradle/libs.versions.toml                      # add ktor + serialization-json; remove retrofit, gson converter, okhttp-logging/mockwebserver
app/build.gradle.kts                           # swap dependencies, apply kotlinSerialization if not in build-logic
app/src/main/java/com/brunodegan/androidplayground/
├── base/network/NetworkModule.kt              # build HttpClient + provide RestApiService
├── data/api/RestApiService.kt                 # keep interface, drop Retrofit annotations, keep constants
├── data/api/KtorRestApiService.kt             # NEW implementation + explicit non-2xx handling
├── data/api/ApiException.kt                   # NEW: status code + body for non-2xx
└── data/datasources/local/entities/ApiEntity.kt  # @SerializedName -> @Serializable/@SerialName
app/src/test/.../data/api/RestApiServiceTest.kt   # MockEngine based
app/src/testFixtures/.../MockUtils.kt             # toJsonString via kotlinx Json
app/proguard-rules / consumer rules               # serialization keep rules if needed
```

**Structure Decision**: Single existing `app` module; no new modules. Interface kept so `RemoteDataSourceImplTest` (MockK on `RestApiService`) needs no change.

## Approach (ordered)

1. Add catalog entries and dependencies; keep Retrofit temporarily so the build stays green.
2. Annotate models with `@Serializable` + `@SerialName`; keep `@Parcelize`. Gson is still used by Room converter, so keep it as a direct dependency.
3. Add `KtorRestApiService` implementing `RestApiService`; remove Retrofit annotations from the interface.
4. Rewrite `NetworkModule`: `HttpClient(OkHttp)` with `defaultRequest` (base URL + headers), `ContentNegotiation(Json { ignoreUnknownKeys = true })`, `HttpTimeout` 60 s, `Logging` at BODY in debug / NONE in release. Single instance via Koin `@Singleton`. No `expectSuccess`; status handling is explicit in `KtorRestApiService` (one private helper that checks `isSuccess()` and throws `ApiException`, then decodes the body).
5. Migrate `RestApiServiceTest` to `MockEngine`; assert URL, query, method, headers, body per endpoint plus error paths: 4xx, 5xx and a 3xx must each throw `ApiException` with the status code.
6. Update `MockUtils.toJsonString` to kotlinx.
7. Remove Retrofit, okhttp-logging, mockwebserver, converter deps and catalog entries; grep for zero leftovers.
8. Verify: unit tests, `lintDebug`, ktlint, `assembleRelease` with shrinking, manual smoke of 4 lists + favorites.

## Risks

- Gson vs kotlinx null/default handling differs: `ignoreUnknownKeys`, `explicitNulls = false`, `coerceInputValues` as needed; request default `media_type` must still be encoded (`encodeDefaults = true`).
- Non-2xx: Retrofit throws `HttpException`; Ktor does not by default. Do NOT use `expectSuccess = true`. Each call checks `response.status.isSuccess()` explicitly and any non-2xx (including 3xx, 1xx) is raised as an error (typed `ApiException` carrying status code and body). Callers already wrap remote calls in `runCatching` (`MoviesRepositoryImpl`), so `ApiException` is handled without changes. Redirects: the client sets `followRedirects = false` so a 3xx reaches the status check and becomes an error in production.
- Release shrinking (R8) with serialization: verify with release build.
- Koin compiler plugin and `@Singleton` function in a `@Module` class: keep the existing pattern.

## Complexity Tracking

No violations.
