# Movie App 🎬

A modern Android application built with Jetpack Compose that allows users to browse, search, and explore movies using The Movie Database (TMDB) API.

## ✨ Features

- **Browse Movies**: View popular, top-rated, and now playing movies
- **Search Functionality**: Search for movies by title
- **Movie Details**: View comprehensive information about each movie including:
  - Synopsis and overview
  - Cast and crew information
  - Release date, runtime, and rating
  - Genres and additional metadata
- **Offline Support**: Cached movie data for offline viewing
- **Modern UI**: Beautiful Material Design 3 interface with smooth animations
- **Responsive Design**: Optimized for different screen sizes

## 📱 Screenshots

<!-- Add screenshots here when available -->

## 🏗️ Architecture

This app follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── data/              # Data layer
│   ├── cache/         # Cache management
│   ├── local/         # Room database
│   │   ├── dao/       # Data Access Objects
│   │   ├── database/  # Database configuration
│   │   ├── entity/    # Database entities
│   │   └── converters/# Type converters
│   ├── mapper/        # Data mappers
│   ├── remote/        # Network layer
│   │   ├── api/       # API interface
│   │   ├── dto/       # Data Transfer Objects
│   │   └── interceptor/# Network interceptors
│   └── repository/    # Repository implementations
├── domain/            # Business logic layer
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Use cases
├── presentation/      # UI layer
│   ├── common/        # Shared UI components
│   ├── detail/        # Movie detail screen
│   ├── home/          # Home screen
│   └── theme/         # App theme
└── di/                # Dependency injection
```

### Architecture Layers

1. **Presentation Layer**: Jetpack Compose UI with ViewModels following MVI pattern
2. **Domain Layer**: Business logic with use cases and domain models
3. **Data Layer**: Repository pattern with local (Room) and remote (Retrofit) data sources

## 🛠️ Tech Stack

### Core
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material Design 3** - UI design system

### Architecture & DI
- **Hilt** - Dependency injection
- **Clean Architecture** - Architectural pattern
- **MVI Pattern** - UI state management

### Networking
- **Retrofit** - REST API client
- **OkHttp** - HTTP client
- **Gson** - JSON serialization

### Database
- **Room** - Local database
- **Coroutines** - Asynchronous programming

### Navigation
- **Compose Destinations** - Type-safe navigation for Compose

### Image Loading
- **Coil** - Image loading library

### Testing
- **JUnit** - Unit testing framework
- **MockK** - Mocking library
- **Truth** - Assertion library
- **Turbine** - Flow testing library
- **Coroutines Test** - Testing coroutines

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK with API 29 or higher
- TMDB API Key

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/I-slam404/Movie_App.git
   cd Movie_App
   ```

2. **Get TMDB API Key**
   - Sign up at [The Movie Database (TMDB)](https://www.themoviedb.org/)
   - Navigate to Settings → API and generate an API key

3. **Configure API Key**
   - Create a `local.properties` file in the root directory (if it doesn't exist)
   - Add your TMDB API key:
     ```properties
     TMDB_API_KEY=your_api_key_here
     ```

4. **Build and Run**
   ```bash
   ./gradlew build
   ```
   
   Or open the project in Android Studio and click Run.

## 🧪 Testing

The app includes comprehensive test coverage:

### Running Tests

**Unit Tests:**
```bash
./gradlew test
```

**Instrumented Tests:**
```bash
./gradlew connectedAndroidTest
```

### Test Coverage

- Repository tests with mocked data sources
- Use case tests with test data
- Mapper tests for data transformation
- Cache manager tests
- ViewModel tests (coming soon)

See [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) and [TESTING_SUMMARY.md](TESTING_SUMMARY.md) for detailed testing information.

## 📦 Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

The release build is configured with:
- ProGuard/R8 code optimization
- Resource shrinking
- Minification enabled

## 🔑 Key Features Implementation

### Caching Strategy
The app implements a smart caching system:
- Movies are cached locally using Room database
- Cache timeout of 30 minutes
- Automatic cache invalidation
- Offline-first approach when data is available

### Network Layer
- Automatic API key injection via interceptor
- Error handling with proper exceptions
- Logging in debug builds

### UI/UX
- Shimmer loading effects
- Error states with retry functionality
- Empty states with helpful messages
- Pull-to-refresh functionality
- Smooth animations and transitions

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Steps to Contribute:
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is available under the MIT License.

## 🙏 Acknowledgments

- [The Movie Database (TMDB)](https://www.themoviedb.org/) for providing the movie data API
- Android development community for excellent libraries and tools

## 📧 Contact

For questions or feedback, please open an issue on GitHub.

---

**Note**: This app uses TMDB API but is not endorsed or certified by TMDB.
