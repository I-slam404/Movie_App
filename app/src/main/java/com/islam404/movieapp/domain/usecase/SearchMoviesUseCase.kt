package com.islam404.movieapp.domain.usecase

import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.domain.repository.MovieRepository
import com.islam404.movieapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(query: String, page: Int = 1): Flow<Resource<List<Movie>>> {
        if (query.isBlank()) {
            return flow {
                emit(Resource.Success(emptyList()))
            }
        }
        return repository.searchMovies(query, page)
    }
}