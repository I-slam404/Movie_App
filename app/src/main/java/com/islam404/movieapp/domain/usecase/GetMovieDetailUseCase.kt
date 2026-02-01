package com.islam404.movieapp.domain.usecase

import com.islam404.movieapp.domain.model.MovieDetail
import com.islam404.movieapp.domain.repository.MovieRepository
import com.islam404.movieapp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    operator fun invoke(movieId: Int): Flow<Resource<MovieDetail>> {
        return repository.getMovieDetail(movieId)
    }
}