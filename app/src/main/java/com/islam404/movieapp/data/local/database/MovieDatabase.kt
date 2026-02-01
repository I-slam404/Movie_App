package com.islam404.movieapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.islam404.movieapp.data.local.converters.MovieListConverter
import com.islam404.movieapp.data.local.dao.MovieCacheDao
import com.islam404.movieapp.data.local.entity.MovieCacheEntity

@Database(
    entities = [
        MovieCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MovieListConverter::class)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieCacheDao(): MovieCacheDao
}