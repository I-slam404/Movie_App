package com.islam404.movieapp.domain.repository

import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.domain.model.MovieDetail
import com.islam404.movieapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getPopularMovies(page: Int = 1, forceRefresh: Boolean = false): Flow<Resource<List<Movie>>>
    fun getTopRatedMovies(page: Int = 1, forceRefresh: Boolean = false): Flow<Resource<List<Movie>>>
    fun getNowPlayingMovies(page: Int = 1, forceRefresh: Boolean = false): Flow<Resource<List<Movie>>>
    fun getMovieDetail(movieId: Int): Flow<Resource<MovieDetail>>
    fun searchMovies(query: String, page: Int = 1): Flow<Resource<List<Movie>>>
    suspend fun clearCache()
}