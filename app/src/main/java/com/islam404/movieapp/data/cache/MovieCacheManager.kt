package com.islam404.movieapp.data.cache

import com.google.gson.Gson
import com.islam404.movieapp.data.local.converters.MovieJson
import com.islam404.movieapp.data.local.dao.MovieCacheDao
import com.islam404.movieapp.data.local.entity.MovieCacheEntity
import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.util.AppLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import java.security.MessageDigest

@Singleton
class MovieCacheManager @Inject constructor(
    private val cacheDao: MovieCacheDao,
    private val gson: Gson
) {
    companion object {
        private const val CACHE_EXPIRATION_TIME = 5 * 60 * 1000L // 5 minutes
        private const val STALE_TIME = 30 * 1000L // 30 seconds - show stale data but refresh
    }

    // Memory cache for instant access
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val mutex = Mutex()

    data class CacheEntry(
        val movies: List<Movie>,
        val timestamp: Long,
        val dataHash: String,
        val hasMore: Boolean
    )

    data class CacheResult(
        val movies: List<Movie>,
        val isStale: Boolean,
        val isExpired: Boolean,
        val hasMore: Boolean
    )

    /**
     * Get cached movies with freshness information
     */
    suspend fun getCachedMovies(category: String, page: Int): CacheResult? = mutex.withLock {
        val cacheKey = getCacheKey(category, page)
        val currentTime = System.currentTimeMillis()

        // Check memory cache first
        memoryCache[cacheKey]?.let { entry ->
            val age = currentTime - entry.timestamp
            AppLogger.d("CacheManager", "Memory cache HIT: $cacheKey (age: ${age}ms)")
            return CacheResult(
                movies = entry.movies,
                isStale = age > STALE_TIME,
                isExpired = age > CACHE_EXPIRATION_TIME,
                hasMore = entry.hasMore
            )
        }

        // Check disk cache
        val diskCache = cacheDao.getCachedMovies(cacheKey)
        if (diskCache != null) {
            val age = currentTime - diskCache.timestamp
            AppLogger.d("CacheManager", "Disk cache HIT: $cacheKey (age: ${age}ms)")

            val movies = deserializeMovies(diskCache.moviesJson)

            // Update memory cache
            memoryCache[cacheKey] = CacheEntry(
                movies = movies,
                timestamp = diskCache.timestamp,
                dataHash = diskCache.dataHash,
                hasMore = diskCache.hasMore
            )

            return CacheResult(
                movies = movies,
                isStale = age > STALE_TIME,
                isExpired = age > CACHE_EXPIRATION_TIME,
                hasMore = diskCache.hasMore
            )
        }

        AppLogger.d("CacheManager", "Cache MISS: $cacheKey")
        return null
    }

    /**
     * Cache movies with metadata
     */
    suspend fun cacheMovies(
        category: String,
        page: Int,
        movies: List<Movie>,
        totalPages: Int = 0,
        hasMore: Boolean = true
    ) = mutex.withLock {
        val cacheKey = getCacheKey(category, page)
        val timestamp = System.currentTimeMillis()
        val moviesJson = serializeMovies(movies)
        val dataHash = calculateHash(moviesJson)

        AppLogger.d("CacheManager", "Caching: $cacheKey (${movies.size} movies, hasMore: $hasMore)")

        // Save to memory cache
        memoryCache[cacheKey] = CacheEntry(
            movies = movies,
            timestamp = timestamp,
            dataHash = dataHash,
            hasMore = hasMore
        )

        // Save to disk cache
        val cacheEntity = MovieCacheEntity(
            cacheKey = cacheKey,
            category = category,
            page = page,
            moviesJson = moviesJson,
            timestamp = timestamp,
            dataHash = dataHash,
            totalPages = totalPages,
            hasMore = hasMore
        )
        cacheDao.cacheMovies(cacheEntity)
    }

    /**
     * Check if data has changed by comparing hash
     */
    suspend fun hasDataChanged(category: String, page: Int, newMovies: List<Movie>): Boolean = mutex.withLock {
        val cacheKey = getCacheKey(category, page)
        val newHash = calculateHash(serializeMovies(newMovies))

        // Check memory cache first
        memoryCache[cacheKey]?.let { entry ->
            return entry.dataHash != newHash
        }

        // Check disk cache
        val diskCache = cacheDao.getCachedMovies(cacheKey)
        return diskCache?.dataHash != newHash
    }

    /**
     * Clear all cache
     */
    suspend fun clearCache() = mutex.withLock {
        AppLogger.d("CacheManager", "Clearing all cache")
        memoryCache.clear()
        cacheDao.clearAllCache()
    }

    /**
     * Clear specific category cache
     */
    suspend fun clearCategory(category: String) = mutex.withLock {
        AppLogger.d("CacheManager", "Clearing cache for category: $category")
        memoryCache.keys.removeAll { it.startsWith("${category}_") }
        cacheDao.deleteCachedCategory(category)
    }

    /**
     * Delete expired cache entries
     */
    suspend fun clearExpiredCache() = mutex.withLock {
        val expirationTimestamp = System.currentTimeMillis() - CACHE_EXPIRATION_TIME
        cacheDao.deleteExpiredCache(expirationTimestamp)

        // Also clear from memory cache
        val keysToRemove = memoryCache.filter { (_, entry) ->
            entry.timestamp < expirationTimestamp
        }.keys
        keysToRemove.forEach { memoryCache.remove(it) }

        AppLogger.d("CacheManager", "Cleared expired cache entries")
    }

    private fun getCacheKey(category: String, page: Int): String {
        return "${category}_page_$page"
    }

    private fun calculateHash(data: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun serializeMovies(movies: List<Movie>): String {
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

    private fun deserializeMovies(json: String): List<Movie> {
        val type = object : com.google.gson.reflect.TypeToken<List<MovieJson>>() {}.type
        val movieJsonList: List<MovieJson> = gson.fromJson(json, type)
        return movieJsonList.map { movieJson ->
            Movie(
                id = movieJson.id,
                title = movieJson.title,
                overview = movieJson.overview,
                posterPath = movieJson.posterPath,
                backdropPath = movieJson.backdropPath,
                releaseDate = movieJson.releaseDate,
                voteAverage = movieJson.voteAverage,
                voteCount = movieJson.voteCount,
                popularity = movieJson.popularity,
                genreIds = movieJson.genreIds
            )
        }
    }
}