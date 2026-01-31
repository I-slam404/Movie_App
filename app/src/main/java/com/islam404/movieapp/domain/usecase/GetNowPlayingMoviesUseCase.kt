package com.islam404.movieapp.domain.usecase


import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.domain.repository.MovieRepository
import com.islam404.movieapp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNowPlayingMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(page: Int = 1, forceRefresh: Boolean = false): Flow<Resource<List<Movie>>> {
        return repository.getNowPlayingMovies(page, forceRefresh)
    }
}