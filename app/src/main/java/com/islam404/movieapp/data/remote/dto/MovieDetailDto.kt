package com.islam404.movieapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    val runtime: Int,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val genres: List<GenreDto>,
    val tagline: String?,
    val status: String,
    val budget: Long,
    val revenue: Long
)

data class GenreDto(
    val id: Int,
    val name: String
)

data class CreditsDto(
    val cast: List<CastDto>
)

data class CastDto(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?
)