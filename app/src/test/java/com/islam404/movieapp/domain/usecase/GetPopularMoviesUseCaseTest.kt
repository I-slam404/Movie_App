package com.islam404.movieapp.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.domain.repository.MovieRepository
import com.islam404.movieapp.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetPopularMoviesUseCase
 * Tests the use case behavior for fetching popular movies
 */
class GetPopularMoviesUseCaseTest {

    private lateinit var useCase: GetPopularMoviesUseCase
    private lateinit var repository: MovieRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetPopularMoviesUseCase(repository)
    }

    @Test
    fun `invoke with default parameters calls repository with default values`() = runTest {
        // Arrange
        val movies = listOf(createTestMovie(1), createTestMovie(2))
        every { repository.getPopularMovies(1, false) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke().test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movies)
            awaitComplete()
        }

        verify { repository.getPopularMovies(1, false) }
    }

    @Test
    fun `invoke with custom page calls repository with correct page`() = runTest {
        // Arrange
        val page = 2
        val movies = listOf(createTestMovie(3), createTestMovie(4))
        every { repository.getPopularMovies(page, false) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(page = page).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movies)
            awaitComplete()
        }

        verify { repository.getPopularMovies(page, false) }
    }

    @Test
    fun `invoke with forceRefresh true calls repository with forceRefresh true`() = runTest {
        // Arrange
        val movies = listOf(createTestMovie(1))
        every { repository.getPopularMovies(1, true) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(forceRefresh = true).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            awaitComplete()
        }

        verify { repository.getPopularMovies(1, true) }
    }

    @Test
    fun `invoke emits loading state from repository`() = runTest {
        // Arrange
        every { repository.getPopularMovies(1, false) } returns flowOf(
            Resource.Loading(),
            Resource.Success(emptyList())
        )

        // Act
        useCase.invoke().test {
            // Assert
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)
            
            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits error state from repository`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        every { repository.getPopularMovies(1, false) } returns flowOf(
            Resource.Error(errorMessage)
        )

        // Act
        useCase.invoke().test {
            // Assert
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).isEqualTo(errorMessage)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when repository returns empty list`() = runTest {
        // Arrange
        every { repository.getPopularMovies(1, false) } returns flowOf(Resource.Success(emptyList()))

        // Act
        useCase.invoke().test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEmpty()
            awaitComplete()
        }
    }

    private fun createTestMovie(id: Int) = Movie(
        id = id,
        title = "Test Movie $id",
        overview = "Test overview",
        posterPath = "/test.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 100,
        popularity = 50.0,
        genreIds = listOf(1, 2)
    )
}
