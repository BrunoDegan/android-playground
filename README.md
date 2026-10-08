# Android Playground

This project is an Android application built using **Kotlin** and **Jetpack Compose**. It showcases a list of favorite movies, allowing users to view details, interact with the UI, and manage their favorite movies. The app follows modern Android development practices, including MVVM architecture, dependency injection with Koin, and Jetpack libraries.

![android_playground_video](https://github.com/user-attachments/assets/8b2f1bf5-cbb3-4ff4-a38f-561c3103c740)

## Features

- Display a list of favorite movies distributed on 4 categories: Top rated, Upcoming, Now Playing and Popular movies
- View detailed information about each movie.
- Add or remove movies from the favorites list.
- Error handling and retry functionality.
- For Top rated, Upcoming, now playing screen and popular movies we applied Local first principle using ROOM database for caching all 4 categories locally
- Fully implemented using Jetpack Compose for UI.
- Unit and instrumented tests for ViewModel and UI.

## Tech Stack

- **Programming Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Koin
- **Networking**: Ktor Client (OkHttp engine, kotlinx.serialization)
- **Image Loading**: Coil
- **Testing**:
  - Unit Tests: JUnit, MockK
  - UI Tests: Jetpack Compose Testing, Espresso, UiAutomator
  - API Tests: Ktor MockEngine

## Project Structure

- `app/src/main/java/com/brunodegan/androidplayground/`
  - **ui**: Contains UI components and screens.
  - **viewModel**: ViewModel classes for managing UI state.
  - **domain**: Use cases for business logic.
  - **data**: Data sources and repository implementations.
  - **base**: Common utilities and base classes.
  - **metrics**: Base metrics layer, with object to handle onEvent and onEnteredScreen created and used
 
## Future

  Add more instrumented tests, refine a better UI, add logic to show favorites movies liked on all BottomNav Pages 

## Known Issues

- Scroll State hasn't been cached after activity reconstruction (screen rotation) (FIXED)

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/BrunoDegan/android-playground.git
   cd android-playground
