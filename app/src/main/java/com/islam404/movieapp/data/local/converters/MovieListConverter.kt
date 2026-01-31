package com.islam404.movieapp.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MovieListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<MovieJson> {
        val listType = object : TypeToken<List<MovieJson>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toString(list: List<MovieJson>): String {
        return gson.toJson(list)
    }
}

data class MovieJson(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val genreIds: List<Int>
)