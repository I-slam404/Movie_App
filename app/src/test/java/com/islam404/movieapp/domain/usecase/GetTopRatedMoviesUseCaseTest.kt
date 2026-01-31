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
 * Unit tests for GetTopRatedMoviesUseCase
 * Tests the use case behavior for fetching top-rated movies
 */
class GetTopRatedMoviesUseCaseTest {

    private lateinit var useCase: GetTopRatedMoviesUseCase
    private lateinit var repository: MovieRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetTopRatedMoviesUseCase(repository)
    }

    @Test
    fun `invoke with default parameters calls repository with default values`() = runTest {
        // Arrange
        val movies = listOf(createTestMovie(1), createTestMovie(2))
        every { repository.getTopRatedMovies(1, false) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke().test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movies)
            awaitComplete()
        }

        verify { repository.getTopRatedMovies(1, false) }
    }

    @Test
    fun `invoke with custom page calls repository with correct page`() = runTest {
        // Arrange
        val page = 3
        val movies = listOf(createTestMovie(5), createTestMovie(6))
        every { repository.getTopRatedMovies(page, false) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(page = page).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movies)
            awaitComplete()
        }

        verify { repository.getTopRatedMovies(page, false) }
    }

    @Test
    fun `invoke with forceRefresh true calls repository with forceRefresh true`() = runTest {
        // Arrange
        val movies = listOf(createTestMovie(1))
        every { repository.getTopRatedMovies(1, true) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(forceRefresh = true).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            awaitComplete()
        }

        verify { repository.getTopRatedMovies(1, true) }
    }

    @Test
    fun `invoke emits error state from repository`() = runTest {
        // Arrange
        val errorMessage = "Failed to fetch top-rated movies"
        every { repository.getTopRatedMovies(1, false) } returns flowOf(
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

    private fun createTestMovie(id: Int) = Movie(
        id = id,
        title = "Top Rated Movie $id",
        overview = "Test overview",
        posterPath = "/test.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 9.0,
        voteCount = 1000,
        popularity = 100.0,
        genreIds = listOf(1, 2)
    )
}
