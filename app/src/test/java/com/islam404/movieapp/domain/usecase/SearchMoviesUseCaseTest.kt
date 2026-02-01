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
 * Unit tests for SearchMoviesUseCase
 * Tests the use case behavior for searching movies, including blank query validation
 */
class SearchMoviesUseCaseTest {

    private lateinit var useCase: SearchMoviesUseCase
    private lateinit var repository: MovieRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchMoviesUseCase(repository)
    }

    @Test
    fun `invoke with valid query calls repository with query`() = runTest {
        // Arrange
        val query = "Inception"
        val movies = listOf(createTestMovie(1, "Inception"))
        every { repository.searchMovies(query, 1) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(query).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movies)
            awaitComplete()
        }

        verify { repository.searchMovies(query, 1) }
    }

    @Test
    fun `invoke with blank query returns empty list without calling repository`() = runTest {
        // Arrange
        val blankQuery = "   "

        // Act
        useCase.invoke(blankQuery).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEmpty()
            awaitComplete()
        }

        // Verify repository was not called for blank query
        verify(exactly = 0) { repository.searchMovies(any(), any()) }
    }

    @Test
    fun `invoke with empty query returns empty list without calling repository`() = runTest {
        // Arrange
        val emptyQuery = ""

        // Act
        useCase.invoke(emptyQuery).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEmpty()
            awaitComplete()
        }

        verify(exactly = 0) { repository.searchMovies(any(), any()) }
    }

    @Test
    fun `invoke with custom page calls repository with correct page`() = runTest {
        // Arrange
        val query = "Matrix"
        val page = 2
        val movies = listOf(createTestMovie(2, "The Matrix Reloaded"))
        every { repository.searchMovies(query, page) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(query, page).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movies)
            awaitComplete()
        }

        verify { repository.searchMovies(query, page) }
    }

    @Test
    fun `invoke emits loading state from repository`() = runTest {
        // Arrange
        val query = "Avengers"
        val movies = listOf(createTestMovie(1, "Avengers"))
        every { repository.searchMovies(query, 1) } returns flowOf(
            Resource.Loading(),
            Resource.Success(movies)
        )

        // Act
        useCase.invoke(query).test {
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
        val query = "Batman"
        val errorMessage = "Search failed"
        every { repository.searchMovies(query, 1) } returns flowOf(
            Resource.Error(errorMessage)
        )

        // Act
        useCase.invoke(query).test {
            // Assert
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).isEqualTo(errorMessage)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when repository returns no results`() = runTest {
        // Arrange
        val query = "NonExistentMovie123"
        every { repository.searchMovies(query, 1) } returns flowOf(Resource.Success(emptyList()))

        // Act
        useCase.invoke(query).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke handles special characters in query`() = runTest {
        // Arrange
        val query = "Spider-Man: No Way Home"
        val movies = listOf(createTestMovie(1, query))
        every { repository.searchMovies(query, 1) } returns flowOf(Resource.Success(movies))

        // Act
        useCase.invoke(query).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).hasSize(1)
            awaitComplete()
        }

        verify { repository.searchMovies(query, 1) }
    }

    @Test
    fun `invoke with whitespace-only query returns empty list`() = runTest {
        // Arrange
        val whitespaceQuery = "\t  \n  "

        // Act
        useCase.invoke(whitespaceQuery).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEmpty()
            awaitComplete()
        }

        verify(exactly = 0) { repository.searchMovies(any(), any()) }
    }

    private fun createTestMovie(id: Int, title: String = "Test Movie $id") = Movie(
        id = id,
        title = title,
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
