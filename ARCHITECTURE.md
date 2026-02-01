# Architecture Documentation

## Overview

The Movie App follows **Clean Architecture** principles, ensuring a scalable, maintainable, and testable codebase. The architecture is divided into three main layers: Presentation, Domain, and Data.

## Clean Architecture Layers

### 1. Presentation Layer (`presentation/`)

The UI layer built with Jetpack Compose, following the MVI (Model-View-Intent) pattern.

#### Components:
- **Screens**: Composable functions representing full screens
- **ViewModels**: Manage UI state and handle user interactions
- **Contracts**: Define UI state, events, and effects for each screen
- **Components**: Reusable UI components

#### Example: Home Screen
```
presentation/home/
├── HomeScreen.kt           # Composable UI
├── HomeViewModel.kt        # Business logic & state management
├── HomeContract.kt         # State, Event, Effect definitions
└── components/             # Screen-specific components
    ├── MovieCard.kt
    ├── MovieList.kt
    └── SearchBar.kt
```

#### State Management (MVI Pattern)
```kotlin
// Contract defines the state
data class HomeState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel manages state
class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
}
```

### 2. Domain Layer (`domain/`)

The business logic layer, independent of frameworks and UI.

#### Components:
- **Models**: Pure Kotlin data classes representing business entities
- **Use Cases**: Single-responsibility business operations
- **Repository Interfaces**: Contracts for data operations

#### Use Cases
Each use case encapsulates a single business operation:

```kotlin
class GetPopularMoviesUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(): Result<List<Movie>> {
        return repository.getPopularMovies()
    }
}
```

**Available Use Cases:**
- `GetPopularMoviesUseCase` - Fetch popular movies
- `GetTopRatedMoviesUseCase` - Fetch top-rated movies
- `GetNowPlayingMoviesUseCase` - Fetch now playing movies
- `GetMovieDetailUseCase` - Fetch movie details
- `SearchMoviesUseCase` - Search movies by query

### 3. Data Layer (`data/`)

Manages data from various sources (network, database, cache).

#### Components:

**Repository Pattern:**
```
data/repository/
└── MovieRepositoryImpl.kt  # Implementation of domain's MovieRepository
```

**Remote Data Source:**
```
data/remote/
├── api/
│   └── TmdbApi.kt         # Retrofit API interface
├── dto/
│   ├── MovieDto.kt        # Network data models
│   └── MovieDetailDto.kt
└── interceptor/
    └── ApiKeyInterceptor.kt # Adds API key to requests
```

**Local Data Source:**
```
data/local/
├── database/
│   └── MovieDatabase.kt   # Room database configuration
├── dao/
│   └── MovieDao.kt        # Data Access Object
├── entity/
│   └── MovieEntity.kt     # Database entities
└── converters/
    └── TypeConverters.kt  # Convert complex types
```

**Data Mappers:**
```
data/mapper/
└── MovieMapper.kt         # Convert between DTOs, Entities, and Models
```

**Cache Management:**
```
data/cache/
└── MovieCacheManager.kt   # Manages cache validity and timestamps
```

## Data Flow

### Reading Data (Example: Get Popular Movies)

```
[UI/Screen] 
    ↓
[ViewModel] 
    ↓ (calls use case)
[GetPopularMoviesUseCase] 
    ↓ (calls repository)
[MovieRepository] 
    ↓ (checks cache)
[MovieCacheManager]
    ↓ (if cache valid)
    ├─→ [Room Database] → [Return cached data]
    ↓ (if cache invalid)
    └─→ [Retrofit API] → [Store in DB] → [Return fresh data]
```

### Caching Strategy

1. **Check Cache**: Repository checks if cached data is valid
2. **Cache Hit**: Return data from Room database
3. **Cache Miss**: Fetch from API, store in database, return data
4. **Cache Timeout**: 30 minutes

```kotlin
// Pseudo-code for caching logic
suspend fun getPopularMovies(): Result<List<Movie>> {
    return if (cacheManager.isPopularMoviesValid()) {
        // Return from database
        database.getPopularMovies().map { it.toDomain() }
    } else {
        // Fetch from API
        val response = api.getPopularMovies()
        database.insertMovies(response.toDomain())
        cacheManager.markPopularMoviesAsValid()
        Result.success(response.toDomain())
    }
}
```

## Dependency Injection (Hilt)

The app uses Hilt for dependency injection, organized into modules:

### Module Structure

**AppModule** (`di/AppModule.kt`):
- Application context
- Database instance
- DAO instances

**NetworkModule** (`di/NetworkModule.kt`):
- Retrofit instance
- OkHttp client
- API interface
- Interceptors

**RepositoryModule** (`di/RepositoryModule.kt`):
- Repository implementations
- Use cases

### Dependency Graph Example

```
@HiltViewModel
HomeViewModel
    ↓ (injects)
GetPopularMoviesUseCase
    ↓ (injects)
MovieRepository
    ↓ (injects)
├─→ TmdbApi (from NetworkModule)
├─→ MovieDao (from AppModule)
└─→ MovieCacheManager
```

## Navigation

The app uses **Compose Destinations** library for type-safe navigation.

### Navigation Structure

```kotlin
@RootGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator
) {
    // Navigate to detail screen
    navigator.navigate(
        MovieDetailScreenDestination(movieId = movieId)
    )
}
```

**Screens:**
- `HomeScreen` - Main screen with movie lists (start destination)
- `MovieDetailScreen` - Movie detail view

## Error Handling

### Result Wrapper
The app uses Kotlin's `Result` type for error handling:

```kotlin
suspend fun getMovieDetail(id: Int): Result<MovieDetail> {
    return try {
        val response = api.getMovieDetail(id)
        Result.success(response.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### UI Error States
ViewModels handle errors and update UI state:

```kotlin
when (result) {
    is Result.Success -> {
        _state.value = state.value.copy(
            movies = result.data,
            isLoading = false
        )
    }
    is Result.Failure -> {
        _state.value = state.value.copy(
            error = result.message,
            isLoading = false
        )
    }
}
```

## Testing Strategy

### Unit Tests
- **Use Cases**: Test business logic with mocked repositories
- **Repositories**: Test data operations with mocked API and DAO
- **Mappers**: Test data transformations
- **Cache Manager**: Test cache validity logic

### Test Structure
```
test/
├── data/
│   ├── mapper/
│   ├── cache/
│   └── repository/
└── domain/
    └── usecase/
```

### Example Test
```kotlin
@Test
fun `getPopularMovies returns success when API call succeeds`() = runTest {
    // Given
    val expectedMovies = listOf(mockMovie)
    coEvery { api.getPopularMovies() } returns mockResponse
    
    // When
    val result = repository.getPopularMovies()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(expectedMovies, result.getOrNull())
}
```

## Key Design Patterns

1. **Repository Pattern**: Abstracts data sources
2. **Use Case Pattern**: Single responsibility for business operations
3. **Mapper Pattern**: Transforms data between layers
4. **Observer Pattern**: StateFlow for reactive UI updates
5. **Dependency Injection**: Loose coupling via Hilt
6. **MVI Pattern**: Unidirectional data flow in UI layer

## Benefits of This Architecture

✅ **Separation of Concerns**: Each layer has a clear responsibility  
✅ **Testability**: Business logic is independent of Android framework  
✅ **Maintainability**: Easy to modify and extend  
✅ **Scalability**: Simple to add new features  
✅ **Reusability**: Components can be reused across the app  
✅ **Independence**: Layers don't depend on implementation details  

## Future Improvements

- Add pagination for movie lists
- Implement favorites/watchlist functionality
- Add user authentication
- Support for TV shows and series
- Advanced filtering and sorting options
- Share movie details functionality
