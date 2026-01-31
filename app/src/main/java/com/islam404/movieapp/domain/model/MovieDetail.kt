package com.islam404.movieapp.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val runtime: Int,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val cast: List<Cast>,
    val tagline: String?,
    val status: String,
    val budget: Long,
    val revenue: Long
)

data class Genre(
    val id: Int,
    val name: String
)

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?
)