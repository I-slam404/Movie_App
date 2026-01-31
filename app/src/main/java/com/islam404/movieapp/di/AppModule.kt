package com.islam404.movieapp.di

import android.app.Application
import androidx.room.Room
import com.islam404.movieapp.data.local.database.MovieDatabase
import com.islam404.movieapp.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.GsonBuilder

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMovieDatabase(app: Application): MovieDatabase {
        return Room.databaseBuilder(
            app,
            MovieDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(false) // For version 2 migration
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieCacheDao(database: MovieDatabase) = database.movieCacheDao()

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
}