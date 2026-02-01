# API Integration Documentation

## Overview

The Movie App integrates with **The Movie Database (TMDB) API** to fetch movie data. This document describes the API integration, endpoints used, and configuration details.

## TMDB API

**Base URL**: `https://api.themoviedb.org/3/`  
**Image Base URL**: `https://image.tmdb.org/t/p/`  
**API Documentation**: [TMDB API Docs](https://developers.themoviedb.org/3)

## Authentication

### API Key Configuration

The API key is configured through `local.properties` and injected into requests automatically.

**Setup:**
1. Create `local.properties` in the project root
2. Add your API key:
   ```properties
   TMDB_API_KEY=your_api_key_here
   ```

**Build Configuration:**
```kotlin
// app/build.gradle.kts
buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
buildConfigField("String", "TMDB_BASE_URL", "\"https://api.themoviedb.org/3/\"")
buildConfigField("String", "TMDB_IMAGE_BASE_URL", "\"https://image.tmdb.org/t/p/\"")
```

### API Key Injection

An OkHttp interceptor automatically adds the API key to all requests:

```kotlin
class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
            .build()
        
        return chain.proceed(
            original.newBuilder().url(url).build()
        )
    }
}
```

## API Endpoints

### 1. Get Popular Movies

**Endpoint**: `GET /movie/popular`

**Description**: Fetches a list of popular movies.

**Parameters**:
- `page` (Int): Page number (default: 1)
- `language` (String): Language code (default: "en-US")
- `certification.lte` (String): Max certification (default: "PG-13")
- `certification_country` (String): Country for certification (default: "US")
- `include_adult` (Boolean): Include adult content (default: false)

**Response**: `MovieResponseDto`

**Example Usage**:
```kotlin
val response = api.getPopularMovies(page = 1)
```

---

### 2. Get Top Rated Movies

**Endpoint**: `GET /movie/top_rated`

**Description**: Fetches a list of top-rated movies.

**Parameters**: Same as Popular Movies

**Response**: `MovieResponseDto`

**Example Usage**:
```kotlin
val response = api.getTopRatedMovies(page = 1)
```

---

### 3. Get Now Playing Movies

**Endpoint**: `GET /movie/now_playing`

**Description**: Fetches movies currently in theaters.

**Parameters**: Same as Popular Movies

**Response**: `MovieResponseDto`

**Example Usage**:
```kotlin
val response = api.getNowPlayingMovies(page = 1)
```

---

### 4. Get Movie Details

**Endpoint**: `GET /movie/{movie_id}`

**Description**: Fetches detailed information about a specific movie.

**Parameters**:
- `movie_id` (Int): The movie ID (path parameter)
- `language` (String): Language code (default: "en-US")

**Response**: `MovieDetailDto`

**Example Usage**:
```kotlin
val movieDetail = api.getMovieDetail(movieId = 550)
```

---

### 5. Get Movie Credits

**Endpoint**: `GET /movie/{movie_id}/credits`

**Description**: Fetches cast and crew information for a movie.

**Parameters**:
- `movie_id` (Int): The movie ID (path parameter)

**Response**: `CreditsDto`

**Example Usage**:
```kotlin
val credits = api.getMovieCredits(movieId = 550)
```

---

### 6. Search Movies

**Endpoint**: `GET /search/movie`

**Description**: Searches for movies by title.

**Parameters**:
- `query` (String): Search query
- `page` (Int): Page number (default: 1)
- `language` (String): Language code (default: "en-US")
- `certification.lte` (String): Max certification (default: "PG-13")
- `certification_country` (String): Country for certification (default: "US")
- `include_adult` (Boolean): Include adult content (default: false)

**Response**: `MovieResponseDto`

**Example Usage**:
```kotlin
val searchResults = api.searchMovies(query = "inception")
```

## Data Models

### MovieResponseDto
```kotlin
data class MovieResponseDto(
    val page: Int,
    val results: List<MovieDto>,
    val total_pages: Int,
    val total_results: Int
)
```

### MovieDto
```kotlin
data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String,
    val vote_average: Double,
    val vote_count: Int,
    val popularity: Double,
    val genre_ids: List<Int>
)
```

### MovieDetailDto
```kotlin
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String,
    val vote_average: Double,
    val vote_count: Int,
    val runtime: Int?,
    val budget: Long,
    val revenue: Long,
    val genres: List<GenreDto>,
    val production_companies: List<ProductionCompanyDto>,
    val tagline: String?
)
```

### CreditsDto
```kotlin
data class CreditsDto(
    val id: Int,
    val cast: List<CastDto>,
    val crew: List<CrewDto>
)
```

## Image URLs

TMDB provides images at various sizes. The app constructs image URLs using:

**Format**: `{IMAGE_BASE_URL}{size}{path}`

**Example**:
```
https://image.tmdb.org/t/p/w500/poster_path.jpg
```

### Poster Sizes
- `w92` - Small thumbnail
- `w154` - Medium thumbnail
- `w185` - Default size
- `w342` - Large
- `w500` - Extra large
- `w780` - HD
- `original` - Original size

### Backdrop Sizes
- `w300` - Small
- `w780` - Medium
- `w1280` - Large
- `original` - Original size

## Network Configuration

### Retrofit Setup

```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.TMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

### OkHttp Setup

```kotlin
@Provides
@Singleton
fun provideOkHttpClient(
    apiKeyInterceptor: ApiKeyInterceptor,
    loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

### Logging Interceptor

Logging is enabled in debug builds:

```kotlin
@Provides
@Singleton
fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
```

## Error Handling

### Network Errors

The app handles various network errors:

1. **No Internet Connection**: Caught at the repository level
2. **HTTP Errors**: 
   - 401: Invalid API key
   - 404: Resource not found
   - 429: Rate limit exceeded
   - 500: Server error
3. **Timeout Errors**: Connection/read/write timeouts
4. **Parsing Errors**: JSON deserialization failures

### Example Error Handling

```kotlin
suspend fun getMovieDetail(id: Int): Result<MovieDetail> {
    return try {
        val response = api.getMovieDetail(id)
        Result.success(response.toMovieDetail())
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> Result.failure(Exception("Invalid API key"))
            404 -> Result.failure(Exception("Movie not found"))
            429 -> Result.failure(Exception("Rate limit exceeded"))
            else -> Result.failure(Exception("Network error: ${e.message}"))
        }
    } catch (e: IOException) {
        Result.failure(Exception("No internet connection"))
    } catch (e: Exception) {
        Result.failure(Exception("Unexpected error: ${e.message}"))
    }
}
```

## Rate Limiting

TMDB API has rate limits:
- **Free tier**: 40 requests per 10 seconds
- **Paid tier**: Higher limits

The app implements:
1. Local caching to reduce API calls
2. Debouncing for search queries
3. Error handling for rate limit errors

## Data Mapping

API responses are mapped to domain models:

```kotlin
fun MovieDto.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average,
        voteCount = vote_count
    )
}

fun MovieDetailDto.toMovieDetail(): MovieDetail {
    return MovieDetail(
        id = id,
        title = title,
        overview = overview,
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average,
        runtime = runtime,
        genres = genres.map { it.toGenre() },
        tagline = tagline
    )
}
```

## Content Filtering

The app filters content to ensure family-friendly results:

**Default Filters**:
- `certification.lte=PG-13`: Max rating PG-13
- `certification_country=US`: US ratings
- `include_adult=false`: Exclude adult content

These filters are applied to all movie list endpoints.

## API Testing

Use the following tools to test API endpoints:

**Postman Collection**: Import TMDB API endpoints  
**cURL Example**:
```bash
curl "https://api.themoviedb.org/3/movie/popular?api_key=YOUR_KEY&page=1"
```

## Best Practices

1. ✅ **Store API key securely**: Never commit to version control
2. ✅ **Use caching**: Reduce unnecessary API calls
3. ✅ **Handle errors gracefully**: Provide user-friendly error messages
4. ✅ **Implement retry logic**: For transient network errors
5. ✅ **Respect rate limits**: Use caching and debouncing
6. ✅ **Use appropriate image sizes**: Don't load original images unnecessarily
7. ✅ **Test with mock data**: Unit tests should not hit the real API

## Troubleshooting

### Common Issues

**Issue**: 401 Unauthorized  
**Solution**: Check that your API key is correctly set in `local.properties`

**Issue**: No images loading  
**Solution**: Verify image URLs are constructed correctly with base URL + size + path

**Issue**: Slow response times  
**Solution**: Check your internet connection and verify the API is not rate-limited

**Issue**: Empty results  
**Solution**: Verify the search query and check if movies match your content filters

## Additional Resources

- [TMDB API Documentation](https://developers.themoviedb.org/3)
- [TMDB API Terms of Use](https://www.themoviedb.org/documentation/api/terms-of-use)
- [API Status](https://status.themoviedb.org/)
- [Community Forum](https://www.themoviedb.org/talk/category/5047958519c29526b50017d6)
