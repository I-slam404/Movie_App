package com.islam404.movieapp.data.cache

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.islam404.movieapp.data.local.converters.MovieJson
import com.islam404.movieapp.data.local.dao.MovieCacheDao
import com.islam404.movieapp.data.local.entity.MovieCacheEntity
import com.islam404.movieapp.domain.model.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MovieCacheManager
 * Tests caching logic including memory cache, disk cache, and serialization
 */
class MovieCacheManagerTest {

    private lateinit var cacheManager: MovieCacheManager
    private lateinit var cacheDao: MovieCacheDao
    private lateinit var gson: Gson

    @Before
    fun setUp() {
        cacheDao = mockk()
        gson = Gson()
        cacheManager = MovieCacheManager(cacheDao, gson)
    }

    // ========== Get Cached Movies Tests ==========

    @Test
    fun `getCachedMovies returns null when no cache exists`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        coEvery { cacheDao.getCachedMovies("popular_page_1") } returns null

        // Act
        val result = cacheManager.getCachedMovies(category, page)

        // Assert
        assertThat(result).isNull()
        coVerify { cacheDao.getCachedMovies("popular_page_1") }
    }

    @Test
    fun `getCachedMovies returns CacheResult from disk cache when memory cache is empty`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        val movies = listOf(createTestMovie(1), createTestMovie(2))
        val moviesJson = serializeMovies(movies, gson)
        val timestamp = System.currentTimeMillis()
        val cacheEntity = MovieCacheEntity(
            cacheKey = "popular_page_1",
            category = category,
            page = page,
            moviesJson = moviesJson,
            timestamp = timestamp,
            dataHash = "",
            totalPages = 10,
            hasMore = true
        )

        coEvery { cacheDao.getCachedMovies("popular_page_1") } returns cacheEntity

        // Act
        val result = cacheManager.getCachedMovies(category, page)

        // Assert
        assertThat(result).isNotNull()
        assertThat(result?.movies).hasSize(2)
        assertThat(result?.movies?.get(0)?.id).isEqualTo(1)
        assertThat(result?.movies?.get(1)?.id).isEqualTo(2)
        assertThat(result?.hasMore).isTrue()
    }

    @Test
    fun `getCachedMovies returns from memory cache on second call`() = runTest {
        // Arrange
        val category = "top_rated"
        val page = 1
        val movies = listOf(createTestMovie(3))
        val moviesJson = serializeMovies(movies, gson)
        val cacheEntity = MovieCacheEntity(
            cacheKey = "top_rated_page_1",
            category = category,
            page = page,
            moviesJson = moviesJson,
            timestamp = System.currentTimeMillis(),
            dataHash = "",
            totalPages = 5,
            hasMore = true
        )

        coEvery { cacheDao.getCachedMovies("top_rated_page_1") } returns cacheEntity

        // Act - First call (loads from disk to memory)
        val firstResult = cacheManager.getCachedMovies(category, page)
        // Act - Second call (should use memory cache)
        val secondResult = cacheManager.getCachedMovies(category, page)

        // Assert
        assertThat(firstResult?.movies).isEqualTo(secondResult?.movies)
        // Disk cache should only be called once
        coVerify(exactly = 1) { cacheDao.getCachedMovies("top_rated_page_1") }
    }

    @Test
    fun `getCachedMovies handles different pages correctly`() = runTest {
        // Arrange
        val category = "popular"
        val page1 = 1
        val page2 = 2
        val movies1 = listOf(createTestMovie(1))
        val movies2 = listOf(createTestMovie(2))

        coEvery { cacheDao.getCachedMovies("popular_page_1") } returns MovieCacheEntity(
            "popular_page_1", category, page1, serializeMovies(movies1, gson),
            System.currentTimeMillis(), "", 10, true
        )
        coEvery { cacheDao.getCachedMovies("popular_page_2") } returns MovieCacheEntity(
            "popular_page_2", category, page2, serializeMovies(movies2, gson),
            System.currentTimeMillis(), "", 10, true
        )

        // Act
        val result1 = cacheManager.getCachedMovies(category, page1)
        val result2 = cacheManager.getCachedMovies(category, page2)

        // Assert
        assertThat(result1?.movies).hasSize(1)
        assertThat(result1?.movies?.get(0)?.id).isEqualTo(1)
        assertThat(result2?.movies).hasSize(1)
        assertThat(result2?.movies?.get(0)?.id).isEqualTo(2)
    }

    @Test
    fun `getCachedMovies detects stale cache correctly`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        val movies = listOf(createTestMovie(1))
        val staleTimestamp = System.currentTimeMillis() - (60 * 1000L) // 60 seconds old
        val cacheEntity = MovieCacheEntity(
            cacheKey = "popular_page_1",
            category = category,
            page = page,
            moviesJson = serializeMovies(movies, gson),
            timestamp = staleTimestamp,
            dataHash = "",
            totalPages = 10,
            hasMore = true
        )

        coEvery { cacheDao.getCachedMovies("popular_page_1") } returns cacheEntity

        // Act
        val result = cacheManager.getCachedMovies(category, page)

        // Assert
        assertThat(result).isNotNull()
        assertThat(result?.isStale).isTrue()
        assertThat(result?.isExpired).isTrue()
    }

    // ========== Cache Movies Tests ==========

    @Test
    fun `cacheMovies saves to both memory and disk cache`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        val movies = listOf(createTestMovie(1), createTestMovie(2))
        val entitySlot = slot<MovieCacheEntity>()

        coEvery { cacheDao.cacheMovies(capture(entitySlot)) } returns Unit
        coEvery { cacheDao.getCachedMovies(any()) } returns null

        // Act
        cacheManager.cacheMovies(category, page, movies, totalPages = 10, hasMore = true)

        // Assert - Verify disk cache was called
        coVerify { cacheDao.cacheMovies(any()) }

        val savedEntity = entitySlot.captured
        assertThat(savedEntity.cacheKey).isEqualTo("popular_page_1")
        assertThat(savedEntity.category).isEqualTo(category)
        assertThat(savedEntity.page).isEqualTo(page)
        assertThat(savedEntity.totalPages).isEqualTo(10)
        assertThat(savedEntity.hasMore).isTrue()

        // Assert - Verify memory cache by retrieving
        val cachedMovies = cacheManager.getCachedMovies(category, page)
        assertThat(cachedMovies?.movies).hasSize(2)
        assertThat(cachedMovies?.movies?.get(0)?.id).isEqualTo(1)
    }

    @Test
    fun `cacheMovies overwrites existing cache`() = runTest {
        // Arrange
        val category = "top_rated"
        val page = 1
        val oldMovies = listOf(createTestMovie(1))
        val newMovies = listOf(createTestMovie(2), createTestMovie(3))

        coEvery { cacheDao.cacheMovies(any()) } returns Unit
        coEvery { cacheDao.getCachedMovies(any()) } returns null

        // Act - Cache old movies
        cacheManager.cacheMovies(category, page, oldMovies)
        // Act - Cache new movies (overwrite)
        cacheManager.cacheMovies(category, page, newMovies)

        // Assert - Should have new movies
        val result = cacheManager.getCachedMovies(category, page)
        assertThat(result?.movies).hasSize(2)
        assertThat(result?.movies?.get(0)?.id).isEqualTo(2)
        assertThat(result?.movies?.get(1)?.id).isEqualTo(3)
    }

    @Test
    fun `cacheMovies handles empty list`() = runTest {
        // Arrange
        val category = "now_playing"
        val page = 1
        val emptyMovies = emptyList<Movie>()

        coEvery { cacheDao.cacheMovies(any()) } returns Unit

        // Act
        cacheManager.cacheMovies(category, page, emptyMovies)

        // Assert
        coVerify { cacheDao.cacheMovies(any()) }
    }

    @Test
    fun `hasDataChanged returns true when data is different`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        val oldMovies = listOf(createTestMovie(1))
        val newMovies = listOf(createTestMovie(2))

        coEvery { cacheDao.cacheMovies(any()) } returns Unit
        coEvery { cacheDao.getCachedMovies(any()) } returns null

        // Cache old movies
        cacheManager.cacheMovies(category, page, oldMovies)

        // Act
        val hasChanged = cacheManager.hasDataChanged(category, page, newMovies)

        // Assert
        assertThat(hasChanged).isTrue()
    }

    @Test
    fun `hasDataChanged returns false when data is same`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        val movies = listOf(createTestMovie(1))

        coEvery { cacheDao.cacheMovies(any()) } returns Unit
        coEvery { cacheDao.getCachedMovies(any()) } returns null

        // Cache movies
        cacheManager.cacheMovies(category, page, movies)

        // Act - Check with same movies
        val hasChanged = cacheManager.hasDataChanged(category, page, movies)

        // Assert
        assertThat(hasChanged).isFalse()
    }

    // ========== Clear Cache Tests ==========

    @Test
    fun `clearCache clears both memory and disk cache`() = runTest {
        // Arrange
        val category = "popular"
        val page = 1
        val movies = listOf(createTestMovie(1))

        coEvery { cacheDao.cacheMovies(any()) } returns Unit
        coEvery { cacheDao.clearAllCache() } returns Unit
        coEvery { cacheDao.getCachedMovies(any()) } returns null

        // Act - Cache some movies first
        cacheManager.cacheMovies(category, page, movies)
        // Act - Clear cache
        cacheManager.clearCache()

        // Assert - Verify disk cache was cleared
        coVerify { cacheDao.clearAllCache() }

        // Assert - Verify memory cache was cleared
        val result = cacheManager.getCachedMovies(category, page)
        assertThat(result).isNull()
    }

    @Test
    fun `clearCategory clears specific category from both caches`() = runTest {
        // Arrange
        val category = "popular"
        val page1 = 1
        val page2 = 2
        val movies = listOf(createTestMovie(1))

        coEvery { cacheDao.cacheMovies(any()) } returns Unit
        coEvery { cacheDao.deleteCachedCategory(category) } returns Unit
        coEvery { cacheDao.getCachedMovies(any()) } returns null

        // Act - Cache movies for multiple pages
        cacheManager.cacheMovies(category, page1, movies)
        cacheManager.cacheMovies(category, page2, movies)
        // Act - Clear category
        cacheManager.clearCategory(category)

        // Assert
        coVerify { cacheDao.deleteCachedCategory(category) }
    }

    // ========== Helper Methods ==========

    private fun createTestMovie(id: Int) = Movie(
        id = id,
        title = "Test Movie $id",
        overview = "Test overview",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 100,
        popularity = 50.0,
        genreIds = listOf(1, 2)
    )

    private fun serializeMovies(movies: List<Movie>, gson: Gson): String {
        val movieJsonList = movies.map { movie ->
            MovieJson(
                id = movie.id,
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                voteCount = movie.voteCount,
                popularity = movie.popularity,
                genreIds = movie.genreIds
            )
        }
        return gson.toJson(movieJsonList)
    }
}