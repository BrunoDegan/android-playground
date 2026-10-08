# Feature Specification: Migrate Networking Layer from Retrofit to Ktor

**Feature Branch**: `feature/moving-from-retrofit-to-ktorfit`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "Create a plan to migrate Retrofit Http client to KTor Http Client in android-playground project. Create new git branch called "feature/moving-from-retrofit-to-ktorfit""

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Movie lists keep working after migration (Priority: P1)

A user opens the app and browses Now Playing, Popular, Top Rated and Upcoming movie lists. Content loads, with Portuguese (pt-BR) metadata, exactly as before the migration.

**Why this priority**: Core read flows of the app. Any regression here is user-visible and blocks release.

**Independent Test**: Launch the app with a valid API token, open each of the four lists, confirm items render with the same data as on the pre-migration build.

**Acceptance Scenarios**:

1. **Given** a valid token and network, **When** the user opens any movie list, **Then** the list shows the same fields (title, poster, rating, etc.) as before the migration.
2. **Given** the request language default, **When** a list is fetched, **Then** results are returned in pt-BR.
3. **Given** a failed request (timeout, no network, server error), **When** the user opens a list, **Then** the app shows the same error/empty behavior as before the migration, without crashing.

---

### User Story 2 - Favorites keep working after migration (Priority: P1)

A user marks a movie as favorite and later views their favorites list. Both actions work against the configured account.

**Why this priority**: Only write flow (POST with body); different request shape from the lists, so it needs its own proof.

**Independent Test**: Add a movie to favorites, open favorites, confirm it appears.

**Acceptance Scenarios**:

1. **Given** a signed-in configured account, **When** the user favorites a movie, **Then** the request succeeds and the response is handled as before.
2. **Given** favorites exist, **When** the user opens favorites, **Then** the list for the configured account is shown.

---

### User Story 3 - Developer sees no leftover legacy networking (Priority: P2)

A developer working on the project finds one HTTP client stack. The old client dependencies, annotations and configuration are gone, and request debugging is still available in debug builds only.

**Why this priority**: Migration value is lost if both stacks stay. Lower than user-facing parity.

**Independent Test**: Search project for legacy client references and build release and debug variants.

**Acceptance Scenarios**:

1. **Given** the migration is complete, **When** the project is searched, **Then** no references to the old client remain in source or build files.
2. **Given** a debug build, **When** a request is made, **Then** request/response bodies are logged; **Given** a release build, **Then** nothing is logged.

---

### Edge Cases

- Request timeout: still 60 seconds for connect and read.
- Every request still carries Accept, Content-Type and Authorization headers.
- Any response outside the 2xx range (including redirects, client and server errors) MUST be treated as an error, never as a successful result.
- Malformed/unknown JSON fields must fail or be tolerated the same way as before.
- Account id and token still come from build configuration, never hard-coded.
- Release build with code shrinking must still work (no missing-class crashes).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST fetch Now Playing, Popular, Top Rated and Upcoming movie lists with identical request URLs, query parameters and a fixed language (pt-BR) as today; language is not a caller parameter.
- **FR-002**: The app MUST add a movie to favorites and fetch favorites for the configured account with identical URLs, method and request body as today.
- **FR-003**: Every request MUST send Accept, Content-Type and Authorization headers as today.
- **FR-003a**: Any non-2xx response MUST surface to callers as an error carrying the status code.
- **FR-004**: Connect and read timeouts MUST remain 60 seconds.
- **FR-005**: Debug builds MUST log request and response bodies; release builds MUST NOT log them.
- **FR-006**: Response parsing MUST produce the same domain data as before for all existing response models.
- **FR-007**: Callers of the API (data sources, repositories, view models) MUST keep their current behavior; changes to their public contracts are limited to what the migration forces.
- **FR-008**: All legacy HTTP client dependencies, version-catalog entries, imports and configuration MUST be removed once migration is complete.
- **FR-009**: Existing automated tests for the API layer MUST be migrated and pass, covering success and error paths for every endpoint.
- **FR-010**: The new client MUST be provided through the existing dependency-injection setup as a single shared instance.

### Key Entities

- **Movie list response**: Paged collection of movies returned by all four list endpoints and by favorites.
- **Add-to-favorites request / response**: Payload identifying the movie and favorite flag, and the acknowledgement returned.
- **API credentials**: Bearer token and account id supplied via build configuration.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the six existing API operations return the same data as on the pre-migration build in a side-by-side manual check.
- **SC-002**: 100% of existing API-layer tests pass after migration, with no tests deleted without replacement.
- **SC-003**: Zero references to the legacy HTTP client remain in the repository.
- **SC-004**: Debug and release builds compile, pass lint, and release app launches and loads a movie list without crash.
- **SC-005**: No user-visible change in screens, error messages, or load behavior reported during verification.

## Assumptions

- Scope is the TMDB REST API service and its network module only; no new endpoints or features.
- The branch name mentions "ktorfit" while the description says Ktor; assumed target is the Ktor HTTP client. Whether to add Ktorfit (annotation-based wrapper over Ktor, closest to the current interface style) is a design decision for the planning phase.
- Existing JSON models and Koin dependency injection stay; serialization library choice is decided in planning.
- Only the Android target is in scope; no multiplatform work.
- Existing build-config values for token and account id are reused.
