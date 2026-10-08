# Research: Retrofit to Ktor

## R1 — Ktor directly vs Ktorfit

- **Decision**: Ktor client directly; hand-written `KtorRestApiService` class, injected directly (the `RestApiService` interface was dropped as unnecessary).
- **Rationale**: Only 6 endpoints, so hand-written calls are ~40 lines. Ktorfit needs a KSP plugin and compiler-plugin version coupling (supports Kotlin >=2.2, KSP >=2.0.2) on top of the Koin compiler plugin and Kotlin 2.4.20, which adds build risk for little code saved. Preserving the interface keeps `RemoteDataSourceImpl` and its MockK test unchanged.
- **Alternatives**: Ktorfit 2.7.5 (annotation interface nearly identical to Retrofit; reconsider if endpoints grow a lot); keeping Retrofit (rejected, goal of the feature).

## R2 — Ktor version and engine

- **Decision**: Ktor 3.5.2, OkHttp engine.
- **Rationale**: Latest at planning time (2026-07-31 release). OkHttp engine is already on the classpath (OkHttp 5.x), same TLS/timeouts behavior and network security config as today. CIO/Android engines rejected: change networking behavior for no gain.
- **Alternatives**: `ktor-client-android` (HttpURLConnection), CIO.

## R3 — JSON serialization

- **Decision**: kotlinx.serialization via `ContentNegotiation`; `Json { ignoreUnknownKeys = true; encodeDefaults = true }`.
- **Rationale**: Ktor's first-class path; plugin `kotlinSerialization` already in the catalog; avoids a Gson dependency on the network path. `encodeDefaults` is required so `AddToFavoritesRequest.mediaType` default (`"movie"`) is sent.
- **Alternatives**: Gson content negotiation via community module (extra non-official dependency; keeps reflection and R8 pitfalls).
- **Impact**: `@SerializedName` becomes `@SerialName`; `MockUtils.toJsonString` must stop using Gson, otherwise snake_case field names break.

## R4 — Logging and headers

- **Decision**: `Logging` plugin (`LogLevel.BODY` debug, `NONE` release); `defaultRequest { url(BASE_URL); header(...) }`.
- **Rationale**: Direct equivalents of `HttpLoggingInterceptor` and the header interceptor. Content-Type header is set by ContentNegotiation for bodies; keep the explicit header for parity (FR-003) and verify in tests.
- **Note**: Authorization token is logged at BODY level only in debug; consider `sanitizeHeader` for Authorization.

## R5 — Timeouts

- **Decision**: `HttpTimeout` with `connectTimeoutMillis` and `requestTimeoutMillis` = 60 000; also socket timeout 60 000 to match OkHttp read timeout.
- **Rationale**: OkHttp `readTimeout` is a per-read idle timeout, closest to `socketTimeoutMillis`; `requestTimeout` is a whole-call cap, which is stricter, so set it generously.

## R6 — Error semantics

- **Decision**: Explicit status check; `expectSuccess` is NOT used. Any response outside 2xx throws `ApiException(statusCode, body)`.
- **Rationale**: Per owner direction, every non-2xx must be an error and handled visibly in our code. `expectSuccess = true` delegates this to Ktor, whose default validator only throws for 3xx/4xx/5xx via `ResponseException`, and hides the policy. An explicit `isSuccess()` check is testable and gives one app-owned exception type. Retrofit threw `HttpException` on non-2xx, so caller behavior (spec User Story 1 scenario 3) is preserved; confirm catch sites during implementation.
- **Redirects**: `followRedirects = false` on the client, otherwise OkHttp would follow a 3xx and the status check would never see it. Tests use `MockEngine`, which does not follow redirects, so the 302 test matches production only with this setting.
- **Callers**: repositories use `runCatching`, so `ApiException` is already handled.
- **Alternatives**: `expectSuccess = true` or `HttpCallValidator` (rejected by owner).

## R7 — Testing

- **Decision**: `ktor-client-mock` `MockEngine`; remove MockWebServer.
- **Rationale**: No socket needed, request inspection (URL, headers, body) is direct, faster tests.

## R8 — Out of scope

- Room `DatabaseTypeConverter` still uses Gson; Gson stays as a direct dependency for it. Not an HTTP client, so FR-008 is unaffected.

## Open items

None blocking. To confirm at implementation: exception catch sites; whether `kotlinSerialization` plugin is already applied via build-logic.
