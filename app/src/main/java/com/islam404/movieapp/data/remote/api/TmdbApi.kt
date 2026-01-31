package com.islam404.movieapp.data.remote.api

import com.islam404.movieapp.data.remote.dto.CreditsDto
import com.islam404.movieapp.data.remote.dto.MovieDetailDto
import com.islam404.movieapp.data.remote.dto.MovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("certification.lte") certificationLte: String = "PG-13",
        @Query("certification_country") certificationCountry: String = "US",
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponseDto

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("certification.lte") certificationLte: String = "PG-13",
        @Query("certification_country") certificationCountry: String = "US",
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponseDto

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("certification.lte") certificationLte: String = "PG-13",
        @Query("certification_country") certificationCountry: String = "US",
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): MovieDetailDto

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int
    ): CreditsDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("certification.lte") certificationLte: String = "PG-13",
        @Query("certification_country") certificationCountry: String = "US",
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponseDto
}