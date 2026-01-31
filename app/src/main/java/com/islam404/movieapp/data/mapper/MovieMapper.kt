package com.islam404.movieapp.data.mapper

import com.islam404.movieapp.data.remote.dto.*
import com.islam404.movieapp.domain.model.*

fun MovieDto.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        genreIds = genreIds
    )
}

fun MovieDetailDto.toMovieDetail(cast: List<Cast>): MovieDetail {
    return MovieDetail(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        runtime = runtime,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.toGenre() },
        cast = cast,
        tagline = tagline,
        status = status,
        budget = budget,
        revenue = revenue
    )
}

fun GenreDto.toGenre(): Genre {
    return Genre(
        id = id,
        name = name
    )
}

fun CastDto.toCast(): Cast {
    return Cast(
        id = id,
        name = name,
        character = character,
        profilePath = profilePath
    )
}