package com.islam404.movieapp.data.local.dao

import androidx.room.*
import com.islam404.movieapp.data.local.entity.MovieCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieCacheDao {

    @Query("SELECT * FROM movie_cache WHERE cacheKey = :cacheKey")
    suspend fun getCachedMovies(cacheKey: String): MovieCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheMovies(cache: MovieCacheEntity)

    @Query("DELETE FROM movie_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteCachedMovies(cacheKey: String)

    @Query("DELETE FROM movie_cache WHERE category = :category")
    suspend fun deleteCachedCategory(category: String)

    @Query("DELETE FROM movie_cache")
    suspend fun clearAllCache()

    @Query("DELETE FROM movie_cache WHERE timestamp < :expirationTimestamp")
    suspend fun deleteExpiredCache(expirationTimestamp: Long)

    @Query("SELECT * FROM movie_cache")
    fun getAllCachedMovies(): Flow<List<MovieCacheEntity>>
}