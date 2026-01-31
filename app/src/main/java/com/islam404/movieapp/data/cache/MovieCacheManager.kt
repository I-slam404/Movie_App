package com.islam404.movieapp.data.cache

import com.google.gson.Gson
import com.islam404.movieapp.data.local.converters.MovieJson
import com.islam404.movieapp.data.local.dao.MovieCacheDao
import com.islam404.movieapp.data.local.entity.MovieCacheEntity
import com.islam404.movieapp.domain.model.Movie
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class MovieCacheManager @Inject constructor(
    private val cacheDao: MovieCacheDao,
    private val gson: Gson
) {
    // Memory cache (faster access)
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val mutex = Mutex()

    data class CacheEntry(
        val movies: List<Movie>,
        val timestamp: Long
    )

    suspend fun getCachedMovies(category: String, page: Int): List<Movie>? = mutex.withLock {
        val cacheKey = getCacheKey(category, page)

        // Check memory cache first (always return if exists, ignore expiration)
        memoryCache[cacheKey]?.let { entry ->
            Log.d("CacheManager", "Memory cache HIT: $cacheKey")
            return entry.movies
        }

        // Check disk cache (always return if exists, ignore expiration)
        val diskCache = cacheDao.getCachedMovies(cacheKey)
        if (diskCache != null) {
            Log.d("CacheManager", "Disk cache HIT: $cacheKey")
            val movies = deserializeMovies(diskCache.moviesJson)
            // Update memory cache
            memoryCache[cacheKey] = CacheEntry(movies, diskCache.timestamp)
            return movies
        }

        Log.d("CacheManager", "Cache MISS: $cacheKey")
        return null
    }

    suspend fun cacheMovies(category: String, page: Int, movies: List<Movie>) = mutex.withLock {
        val cacheKey = getCacheKey(category, page)
        val timestamp = System.currentTimeMillis()

        Log.d("CacheManager", "Caching: $cacheKey (${movies.size} movies)")

        // Save to memory cache
        memoryCache[cacheKey] = CacheEntry(movies, timestamp)

        // Save to disk cache
        val moviesJson = serializeMovies(movies)
        val cacheEntity = MovieCacheEntity(
            cacheKey = cacheKey,
            category = category,
            page = page,
            moviesJson = moviesJson,
            timestamp = timestamp
        )
        cacheDao.cacheMovies(cacheEntity)
    }

    suspend fun clearCache() = mutex.withLock {
        Log.d("CacheManager", "Clearing all cache")
        memoryCache.clear()
        cacheDao.clearAllCache()
    }

    suspend fun clearCategory(category: String) = mutex.withLock {
        Log.d("CacheManager", "Clearing cache for category: $category")
        memoryCache.keys.removeAll { it.startsWith("${category}_") }
        cacheDao.deleteCachedCategory(category)
    }

    private fun getCacheKey(category: String, page: Int): String {
        return "${category}_$page"
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