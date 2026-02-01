package com.islam404.movieapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.islam404.movieapp.data.local.converters.MovieListConverter

@Entity(tableName = "movie_cache")
@TypeConverters(MovieListConverter::class)
data class MovieCacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val category: String,
    val page: Int,
    val moviesJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dataHash: String = "",
    val totalPages: Int = 0,
    val hasMore: Boolean = true
)