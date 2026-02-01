package com.islam404.movieapp.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.islam404.movieapp.domain.model.Cast
import com.islam404.movieapp.domain.model.Genre
import com.islam404.movieapp.domain.model.MovieDetail
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
 * Unit tests for GetMovieDetailUseCase
 * Tests the use case behavior for fetching movie details
 */
class GetMovieDetailUseCaseTest {

    private lateinit var useCase: GetMovieDetailUseCase
    private lateinit var repository: MovieRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetMovieDetailUseCase(repository)
    }

    @Test
    fun `invoke with movieId calls repository with correct movieId`() = runTest {
        // Arrange
        val movieId = 123
        val movieDetail = createTestMovieDetail(movieId)
        every { repository.getMovieDetail(movieId) } returns flowOf(Resource.Success(movieDetail))

        // Act
        useCase.invoke(movieId).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).isEqualTo(movieDetail)
            assertThat(result.data?.id).isEqualTo(movieId)
            awaitComplete()
        }

        verify { repository.getMovieDetail(movieId) }
    }

    @Test
    fun `invoke emits loading state from repository`() = runTest {
        // Arrange
        val movieId = 456
        val movieDetail = createTestMovieDetail(movieId)
        every { repository.getMovieDetail(movieId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(movieDetail)
        )

        // Act
        useCase.invoke(movieId).test {
            // Assert
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)
            
            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).isEqualTo(movieDetail)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits error state from repository`() = runTest {
        // Arrange
        val movieId = 789
        val errorMessage = "Movie not found"
        every { repository.getMovieDetail(movieId) } returns flowOf(
            Resource.Error(errorMessage)
        )

        // Act
        useCase.invoke(movieId).test {
            // Assert
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).isEqualTo(errorMessage)
            awaitComplete()
        }
    }

    @Test
    fun `invoke handles different movieIds correctly`() = runTest {
        // Arrange
        val movieId1 = 111
        val movieId2 = 222
        val movieDetail1 = createTestMovieDetail(movieId1)
        val movieDetail2 = createTestMovieDetail(movieId2)
        
        every { repository.getMovieDetail(movieId1) } returns flowOf(Resource.Success(movieDetail1))
        every { repository.getMovieDetail(movieId2) } returns flowOf(Resource.Success(movieDetail2))

        // Act & Assert
        useCase.invoke(movieId1).test {
            val result = awaitItem()
            assertThat(result.data?.id).isEqualTo(movieId1)
            awaitComplete()
        }

        useCase.invoke(movieId2).test {
            val result = awaitItem()
            assertThat(result.data?.id).isEqualTo(movieId2)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns movie detail with all fields populated`() = runTest {
        // Arrange
        val movieId = 333
        val genres = listOf(Genre(1, "Action"), Genre(2, "Adventure"))
        val cast = listOf(
            Cast(1, "Actor 1", "Character 1", "/path1.jpg"),
            Cast(2, "Actor 2", "Character 2", null)
        )
        val movieDetail = MovieDetail(
            id = movieId,
            title = "Test Movie",
            overview = "Test overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-01",
            runtime = 120,
            voteAverage = 8.5,
            voteCount = 1000,
            genres = genres,
            cast = cast,
            tagline = "Test tagline",
            status = "Released",
            budget = 1000000,
            revenue = 5000000
        )
        every { repository.getMovieDetail(movieId) } returns flowOf(Resource.Success(movieDetail))

        // Act
        useCase.invoke(movieId).test {
            // Assert
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data?.title).isEqualTo("Test Movie")
            assertThat(result.data?.genres).hasSize(2)
            assertThat(result.data?.cast).hasSize(2)
            assertThat(result.data?.runtime).isEqualTo(120)
            awaitComplete()
        }
    }

    private fun createTestMovieDetail(movieId: Int) = MovieDetail(
        id = movieId,
        title = "Movie $movieId",
        overview = "Overview for movie $movieId",
        posterPath = "/poster$movieId.jpg",
        backdropPath = "/backdrop$movieId.jpg",
        releaseDate = "2024-01-01",
        runtime = 120,
        voteAverage = 8.0,
        voteCount = 500,
        genres = listOf(Genre(1, "Action")),
        cast = listOf(Cast(1, "Actor", "Character", null)),
        tagline = "Tagline",
        status = "Released",
        budget = 1000000,
        revenue = 5000000
    )
}
