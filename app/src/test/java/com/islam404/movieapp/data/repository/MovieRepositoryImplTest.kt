package com.islam404.movieapp.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.islam404.movieapp.data.cache.MovieCacheManager
import com.islam404.movieapp.data.remote.api.TmdbApi
import com.islam404.movieapp.data.remote.dto.*
import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Unit tests for MovieRepositoryImpl
 * Tests repository behavior including caching, error handling, and data fetching
 */
class MovieRepositoryImplTest {

    private lateinit var repository: MovieRepositoryImpl
    private lateinit var api: TmdbApi
    private lateinit var cacheManager: MovieCacheManager

    @Before
    fun setUp() {
        api = mockk()
        cacheManager = mockk()
        repository = MovieRepositoryImpl(api, cacheManager)
    }

    // ========== Popular Movies Tests ==========

    @Test
    fun `getPopularMovies emits cached data first when cache exists`() = runTest {
        // Arrange
        val cachedMovies = listOf(createTestMovie(1))
        val apiMovies = listOf(createTestMovie(1), createTestMovie(2))
        val movieDtos = apiMovies.map { createMovieDto(it.id) }
        val responseDto = MovieResponseDto(1, movieDtos, 1, 2)

        coEvery { cacheManager.getCachedMovies("popular", 1) } returns cachedMovies
        coEvery { api.getPopularMovies(1) } returns responseDto
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act & Assert
        repository.getPopularMovies(1, false).test {
            // First emission: cached data
            val cached = awaitItem()
            assertThat(cached).isInstanceOf(Resource.Success::class.java)
            assertThat(cached.data).hasSize(1)

            // Second emission: loading with cached data
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)
            assertThat(loading.data).isEqualTo(cachedMovies)

            // Third emission: fresh data from API
            val fresh = awaitItem()
            assertThat(fresh).isInstanceOf(Resource.Success::class.java)
            assertThat(fresh.data).hasSize(2)

            awaitComplete()
        }

        coVerify { cacheManager.getCachedMovies("popular", 1) }
        coVerify { api.getPopularMovies(1) }
        coVerify { cacheManager.cacheMovies("popular", 1, any()) }
    }

    @Test
    fun `getPopularMovies fetches from API when cache is empty`() = runTest {
        // Arrange
        val apiMovies = listOf(createTestMovie(1), createTestMovie(2))
        val movieDtos = apiMovies.map { createMovieDto(it.id) }
        val responseDto = MovieResponseDto(1, movieDtos, 1, 2)

        coEvery { cacheManager.getCachedMovies("popular", 1) } returns null
        coEvery { api.getPopularMovies(1) } returns responseDto
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act & Assert
        repository.getPopularMovies(1, false).test {
            // First emission: loading
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Second emission: success with API data
            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(2)

            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies handles HTTP exception and returns cached data`() = runTest {
        // Arrange
        val cachedMovies = listOf(createTestMovie(1))
        val httpException = mockk<HttpException> {
            coEvery { localizedMessage } returns "HTTP 500 Error"
        }

        coEvery { cacheManager.getCachedMovies("popular", 1) } returns cachedMovies
        coEvery { api.getPopularMovies(1) } throws httpException
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act & Assert
        repository.getPopularMovies(1, false).test {
            // Cached data
            val cached = awaitItem()
            assertThat(cached).isInstanceOf(Resource.Success::class.java)

            // Loading state
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Error with cached data
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.data).isEqualTo(cachedMovies)
            assertThat(error.message).isNotEmpty()

            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies handles IOException with network error message`() = runTest {
        // Arrange
        coEvery { cacheManager.getCachedMovies("popular", 1) } returns null
        coEvery { api.getPopularMovies(1) } throws IOException("Network error")

        // Act & Assert
        repository.getPopularMovies(1, false).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).contains("internet connection")
            assertThat(error.data).isNull()

            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies handles page parameter correctly`() = runTest {
        // Arrange
        val page = 3
        val movieDtos = listOf(createMovieDto(1))
        val responseDto = MovieResponseDto(page, movieDtos, 10, 100)

        coEvery { cacheManager.getCachedMovies("popular", page) } returns null
        coEvery { api.getPopularMovies(page) } returns responseDto
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act & Assert
        repository.getPopularMovies(page, false).test {
            skipItems(1) // Skip loading
            awaitItem() // Success
            awaitComplete()
        }

        coVerify { api.getPopularMovies(page) }
        coVerify { cacheManager.cacheMovies("popular", page, any()) }
    }

    // ========== Top Rated Movies Tests ==========

    @Test
    fun `getTopRatedMovies fetches from API successfully`() = runTest {
        // Arrange
        val movieDtos = listOf(createMovieDto(1), createMovieDto(2))
        val responseDto = MovieResponseDto(1, movieDtos, 1, 2)

        coEvery { cacheManager.getCachedMovies("top_rated", 1) } returns null
        coEvery { api.getTopRatedMovies(1) } returns responseDto
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act & Assert
        repository.getTopRatedMovies(1, false).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(2)

            awaitComplete()
        }

        coVerify { cacheManager.cacheMovies("top_rated", 1, any()) }
    }

    @Test
    fun `getTopRatedMovies uses correct category for caching`() = runTest {
        // Arrange
        val movieDtos = listOf(createMovieDto(1))
        val responseDto = MovieResponseDto(1, movieDtos, 1, 1)

        coEvery { cacheManager.getCachedMovies("top_rated", 1) } returns null
        coEvery { api.getTopRatedMovies(1) } returns responseDto
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act
        repository.getTopRatedMovies(1, false).test {
            skipItems(2) // Skip loading and success
            awaitComplete()
        }

        // Assert
        coVerify { cacheManager.getCachedMovies("top_rated", 1) }
        coVerify { cacheManager.cacheMovies("top_rated", 1, any()) }
    }

    // ========== Now Playing Movies Tests ==========

    @Test
    fun `getNowPlayingMovies fetches from API successfully`() = runTest {
        // Arrange
        val movieDtos = listOf(createMovieDto(1))
        val responseDto = MovieResponseDto(1, movieDtos, 1, 1)

        coEvery { cacheManager.getCachedMovies("now_playing", 1) } returns null
        coEvery { api.getNowPlayingMovies(1) } returns responseDto
        coEvery { cacheManager.cacheMovies(any(), any(), any()) } returns Unit

        // Act & Assert
        repository.getNowPlayingMovies(1, false).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)

            awaitComplete()
        }
    }

    // ========== Movie Detail Tests ==========

    @Test
    fun `getMovieDetail fetches from API successfully with cast`() = runTest {
        // Arrange
        val movieId = 123
        val movieDetailDto = createMovieDetailDto(movieId)
        val creditsDto = CreditsDto(
            cast = listOf(
                CastDto(1, "Actor 1", "Character 1", "/path1.jpg"),
                CastDto(2, "Actor 2", "Character 2", null)
            )
        )

        coEvery { api.getMovieDetail(movieId) } returns movieDetailDto
        coEvery { api.getMovieCredits(movieId) } returns creditsDto

        // Act & Assert
        repository.getMovieDetail(movieId).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data?.id).isEqualTo(movieId)
            assertThat(success.data?.cast).hasSize(2)

            awaitComplete()
        }
    }

    @Test
    fun `getMovieDetail limits cast to 10 members`() = runTest {
        // Arrange
        val movieId = 456
        val movieDetailDto = createMovieDetailDto(movieId)
        val largeCastList = (1..20).map { 
            CastDto(it, "Actor $it", "Character $it", null)
        }
        val creditsDto = CreditsDto(cast = largeCastList)

        coEvery { api.getMovieDetail(movieId) } returns movieDetailDto
        coEvery { api.getMovieCredits(movieId) } returns creditsDto

        // Act & Assert
        repository.getMovieDetail(movieId).test {
            skipItems(1) // Skip loading
            
            val success = awaitItem()
            assertThat(success.data?.cast).hasSize(10)

            awaitComplete()
        }
    }

    @Test
    fun `getMovieDetail handles HTTP exception`() = runTest {
        // Arrange
        val movieId = 789
        val httpException = mockk<HttpException> {
            coEvery { localizedMessage } returns "404 Not Found"
        }

        coEvery { api.getMovieDetail(movieId) } throws httpException

        // Act & Assert
        repository.getMovieDetail(movieId).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).isNotEmpty()

            awaitComplete()
        }
    }

    @Test
    fun `getMovieDetail handles IOException`() = runTest {
        // Arrange
        val movieId = 999
        coEvery { api.getMovieDetail(movieId) } throws IOException("Connection timeout")

        // Act & Assert
        repository.getMovieDetail(movieId).test {
            skipItems(1) // Skip loading

            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).contains("server")

            awaitComplete()
        }
    }

    // ========== Search Movies Tests ==========

    @Test
    fun `searchMovies fetches from API successfully`() = runTest {
        // Arrange
        val query = "Inception"
        val movieDtos = listOf(createMovieDto(1), createMovieDto(2))
        val responseDto = MovieResponseDto(1, movieDtos, 1, 2)

        coEvery { api.searchMovies(query, 1) } returns responseDto

        // Act & Assert
        repository.searchMovies(query, 1).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(2)

            awaitComplete()
        }
    }

    @Test
    fun `searchMovies handles page parameter correctly`() = runTest {
        // Arrange
        val query = "Matrix"
        val page = 2
        val movieDtos = listOf(createMovieDto(1))
        val responseDto = MovieResponseDto(page, movieDtos, 5, 50)

        coEvery { api.searchMovies(query, page) } returns responseDto

        // Act
        repository.searchMovies(query, page).test {
            skipItems(2) // Skip loading and success
            awaitComplete()
        }

        // Assert
        coVerify { api.searchMovies(query, page) }
    }

    @Test
    fun `searchMovies handles HTTP exception`() = runTest {
        // Arrange
        val query = "Test"
        val httpException = mockk<HttpException> {
            coEvery { localizedMessage } returns "Bad Request"
        }

        coEvery { api.searchMovies(query, 1) } throws httpException

        // Act & Assert
        repository.searchMovies(query, 1).test {
            skipItems(1) // Skip loading

            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)

            awaitComplete()
        }
    }

    @Test
    fun `searchMovies handles empty results`() = runTest {
        // Arrange
        val query = "NonExistentMovie"
        val responseDto = MovieResponseDto(1, emptyList(), 1, 0)

        coEvery { api.searchMovies(query, 1) } returns responseDto

        // Act & Assert
        repository.searchMovies(query, 1).test {
            skipItems(1) // Skip loading

            val success = awaitItem()
            assertThat(success.data).isEmpty()

            awaitComplete()
        }
    }

    // ========== Clear Cache Tests ==========

    @Test
    fun `clearCache calls cache manager clearCache`() = runTest {
        // Arrange
        coEvery { cacheManager.clearCache() } returns Unit

        // Act
        repository.clearCache()

        // Assert
        coVerify { cacheManager.clearCache() }
    }

    // ========== Helper Methods ==========

    private fun createTestMovie(id: Int) = Movie(
        id = id,
        title = "Test Movie $id",
        overview = "Overview",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 100,
        popularity = 50.0,
        genreIds = listOf(1, 2)
    )

    private fun createMovieDto(id: Int) = MovieDto(
        id = id,
        title = "Test Movie $id",
        overview = "Overview",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 100,
        popularity = 50.0,
        genreIds = listOf(1, 2)
    )

    private fun createMovieDetailDto(id: Int) = MovieDetailDto(
        id = id,
        title = "Movie Detail $id",
        overview = "Detailed overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-01",
        runtime = 120,
        voteAverage = 8.0,
        voteCount = 500,
        genres = listOf(GenreDto(1, "Action")),
        tagline = "Tagline",
        status = "Released",
        budget = 1000000,
        revenue = 5000000
    )
}
