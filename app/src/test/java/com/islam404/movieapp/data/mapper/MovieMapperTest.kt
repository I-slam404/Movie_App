package com.islam404.movieapp.data.mapper

import com.google.common.truth.Truth.assertThat
import com.islam404.movieapp.data.remote.dto.CastDto
import com.islam404.movieapp.data.remote.dto.GenreDto
import com.islam404.movieapp.data.remote.dto.MovieDetailDto
import com.islam404.movieapp.data.remote.dto.MovieDto
import com.islam404.movieapp.domain.model.Cast
import com.islam404.movieapp.domain.model.Genre
import org.junit.Test

/**
 * Unit tests for MovieMapper extension functions
 * Tests proper mapping from DTO objects to domain models
 */
class MovieMapperTest {

    @Test
    fun `toMovie maps MovieDto to Movie correctly with all fields`() {
        // Arrange
        val movieDto = MovieDto(
            id = 123,
            title = "Test Movie",
            overview = "This is a test overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-15",
            voteAverage = 8.5,
            voteCount = 1500,
            popularity = 75.5,
            genreIds = listOf(28, 12, 16)
        )

        // Act
        val movie = movieDto.toMovie()

        // Assert
        assertThat(movie.id).isEqualTo(123)
        assertThat(movie.title).isEqualTo("Test Movie")
        assertThat(movie.overview).isEqualTo("This is a test overview")
        assertThat(movie.posterPath).isEqualTo("/poster.jpg")
        assertThat(movie.backdropPath).isEqualTo("/backdrop.jpg")
        assertThat(movie.releaseDate).isEqualTo("2024-01-15")
        assertThat(movie.voteAverage).isEqualTo(8.5)
        assertThat(movie.voteCount).isEqualTo(1500)
        assertThat(movie.popularity).isEqualTo(75.5)
        assertThat(movie.genreIds).containsExactly(28, 12, 16).inOrder()
    }

    @Test
    fun `toMovie handles null posterPath correctly`() {
        // Arrange
        val movieDto = MovieDto(
            id = 456,
            title = "Movie Without Poster",
            overview = "Overview",
            posterPath = null,
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 7.0,
            voteCount = 500,
            popularity = 50.0,
            genreIds = listOf(1, 2)
        )

        // Act
        val movie = movieDto.toMovie()

        // Assert
        assertThat(movie.posterPath).isNull()
        assertThat(movie.id).isEqualTo(456)
        assertThat(movie.title).isEqualTo("Movie Without Poster")
    }

    @Test
    fun `toMovie handles null backdropPath correctly`() {
        // Arrange
        val movieDto = MovieDto(
            id = 789,
            title = "Movie Without Backdrop",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = null,
            releaseDate = "2024-01-01",
            voteAverage = 6.5,
            voteCount = 300,
            popularity = 40.0,
            genreIds = emptyList()
        )

        // Act
        val movie = movieDto.toMovie()

        // Assert
        assertThat(movie.backdropPath).isNull()
        assertThat(movie.posterPath).isEqualTo("/poster.jpg")
    }

    @Test
    fun `toMovie handles empty genre list correctly`() {
        // Arrange
        val movieDto = MovieDto(
            id = 111,
            title = "Movie Without Genres",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 5.5,
            voteCount = 100,
            popularity = 20.0,
            genreIds = emptyList()
        )

        // Act
        val movie = movieDto.toMovie()

        // Assert
        assertThat(movie.genreIds).isEmpty()
    }

    @Test
    fun `toMovieDetail maps MovieDetailDto to MovieDetail correctly with all fields`() {
        // Arrange
        val genreDtos = listOf(
            GenreDto(28, "Action"),
            GenreDto(12, "Adventure")
        )
        val movieDetailDto = MovieDetailDto(
            id = 123,
            title = "Test Movie Detail",
            overview = "Detailed overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-15",
            runtime = 148,
            voteAverage = 8.7,
            voteCount = 2500,
            genres = genreDtos,
            tagline = "The best movie ever",
            status = "Released",
            budget = 200000000,
            revenue = 1000000000
        )
        val cast = listOf(
            Cast(1, "Actor One", "Hero", "/actor1.jpg"),
            Cast(2, "Actor Two", "Villain", null)
        )

        // Act
        val movieDetail = movieDetailDto.toMovieDetail(cast)

        // Assert
        assertThat(movieDetail.id).isEqualTo(123)
        assertThat(movieDetail.title).isEqualTo("Test Movie Detail")
        assertThat(movieDetail.overview).isEqualTo("Detailed overview")
        assertThat(movieDetail.posterPath).isEqualTo("/poster.jpg")
        assertThat(movieDetail.backdropPath).isEqualTo("/backdrop.jpg")
        assertThat(movieDetail.releaseDate).isEqualTo("2024-01-15")
        assertThat(movieDetail.runtime).isEqualTo(148)
        assertThat(movieDetail.voteAverage).isEqualTo(8.7)
        assertThat(movieDetail.voteCount).isEqualTo(2500)
        assertThat(movieDetail.genres).hasSize(2)
        assertThat(movieDetail.genres[0].name).isEqualTo("Action")
        assertThat(movieDetail.genres[1].name).isEqualTo("Adventure")
        assertThat(movieDetail.cast).hasSize(2)
        assertThat(movieDetail.tagline).isEqualTo("The best movie ever")
        assertThat(movieDetail.status).isEqualTo("Released")
        assertThat(movieDetail.budget).isEqualTo(200000000)
        assertThat(movieDetail.revenue).isEqualTo(1000000000)
    }

    @Test
    fun `toMovieDetail handles null tagline correctly`() {
        // Arrange
        val movieDetailDto = MovieDetailDto(
            id = 456,
            title = "Movie Without Tagline",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-01",
            runtime = 120,
            voteAverage = 7.5,
            voteCount = 1000,
            genres = emptyList(),
            tagline = null,
            status = "Released",
            budget = 100000000,
            revenue = 500000000
        )

        // Act
        val movieDetail = movieDetailDto.toMovieDetail(emptyList())

        // Assert
        assertThat(movieDetail.tagline).isNull()
    }

    @Test
    fun `toMovieDetail handles empty cast list correctly`() {
        // Arrange
        val movieDetailDto = MovieDetailDto(
            id = 789,
            title = "Movie Without Cast",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-01-01",
            runtime = 90,
            voteAverage = 6.0,
            voteCount = 500,
            genres = listOf(GenreDto(18, "Drama")),
            tagline = "A movie",
            status = "Released",
            budget = 50000000,
            revenue = 250000000
        )

        // Act
        val movieDetail = movieDetailDto.toMovieDetail(emptyList())

        // Assert
        assertThat(movieDetail.cast).isEmpty()
    }

    @Test
    fun `toGenre maps GenreDto to Genre correctly`() {
        // Arrange
        val genreDto = GenreDto(28, "Action")

        // Act
        val genre = genreDto.toGenre()

        // Assert
        assertThat(genre.id).isEqualTo(28)
        assertThat(genre.name).isEqualTo("Action")
    }

    @Test
    fun `toCast maps CastDto to Cast correctly with all fields`() {
        // Arrange
        val castDto = CastDto(
            id = 123,
            name = "John Doe",
            character = "Superhero",
            profilePath = "/profile.jpg"
        )

        // Act
        val cast = castDto.toCast()

        // Assert
        assertThat(cast.id).isEqualTo(123)
        assertThat(cast.name).isEqualTo("John Doe")
        assertThat(cast.character).isEqualTo("Superhero")
        assertThat(cast.profilePath).isEqualTo("/profile.jpg")
    }

    @Test
    fun `toCast handles null profilePath correctly`() {
        // Arrange
        val castDto = CastDto(
            id = 456,
            name = "Jane Smith",
            character = "Villain",
            profilePath = null
        )

        // Act
        val cast = castDto.toCast()

        // Assert
        assertThat(cast.profilePath).isNull()
        assertThat(cast.name).isEqualTo("Jane Smith")
        assertThat(cast.character).isEqualTo("Villain")
    }

    @Test
    fun `toMovie handles zero values correctly`() {
        // Arrange
        val movieDto = MovieDto(
            id = 0,
            title = "Zero Values Movie",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = "",
            voteAverage = 0.0,
            voteCount = 0,
            popularity = 0.0,
            genreIds = emptyList()
        )

        // Act
        val movie = movieDto.toMovie()

        // Assert
        assertThat(movie.id).isEqualTo(0)
        assertThat(movie.voteAverage).isEqualTo(0.0)
        assertThat(movie.voteCount).isEqualTo(0)
        assertThat(movie.popularity).isEqualTo(0.0)
    }

    @Test
    fun `toMovieDetail handles null poster and backdrop paths correctly`() {
        // Arrange
        val movieDetailDto = MovieDetailDto(
            id = 999,
            title = "Movie Without Images",
            overview = "Overview",
            posterPath = null,
            backdropPath = null,
            releaseDate = "2024-01-01",
            runtime = 100,
            voteAverage = 7.0,
            voteCount = 800,
            genres = listOf(GenreDto(18, "Drama")),
            tagline = "Tagline",
            status = "Released",
            budget = 10000000,
            revenue = 50000000
        )

        // Act
        val movieDetail = movieDetailDto.toMovieDetail(emptyList())

        // Assert
        assertThat(movieDetail.posterPath).isNull()
        assertThat(movieDetail.backdropPath).isNull()
    }
}
