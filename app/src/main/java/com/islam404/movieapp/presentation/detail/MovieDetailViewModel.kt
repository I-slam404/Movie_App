package com.islam404.movieapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.domain.usecase.GetMovieDetailUseCase
import com.islam404.movieapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    // State
    private val _state = MutableStateFlow(MovieDetailContract.State())
    val state: StateFlow<MovieDetailContract.State> = _state.asStateFlow()

    // Effects
    private val _effect = Channel<MovieDetailContract.Effect>(Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()

    init {
        onEvent(MovieDetailContract.Event.LoadMovieDetail(movieId))
    }

    fun onEvent(event: MovieDetailContract.Event) {
        when (event) {
            is MovieDetailContract.Event.LoadMovieDetail -> loadMovieDetail(event.movieId)
            is MovieDetailContract.Event.ToggleFavorite -> toggleFavorite()
            is MovieDetailContract.Event.NavigateBack -> navigateBack()
            is MovieDetailContract.Event.Retry -> loadMovieDetail(movieId)
        }
    }

    private fun loadMovieDetail(movieId: Int) {
        viewModelScope.launch {
            getMovieDetailUseCase(movieId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                movieDetail = result.data,
                                isFavorite = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _effect.send(
                            MovieDetailContract.Effect.ShowError(
                                result.message ?: "Unknown error"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val movieDetail = _state.value.movieDetail ?: return@launch

            // Convert MovieDetail to Movie for favorites
            val movie = Movie(
                id = movieDetail.id,
                title = movieDetail.title,
                overview = movieDetail.overview,
                posterPath = movieDetail.posterPath,
                backdropPath = movieDetail.backdropPath,
                releaseDate = movieDetail.releaseDate,
                voteAverage = movieDetail.voteAverage,
                voteCount = movieDetail.voteCount,
                popularity = 0.0,
                genreIds = movieDetail.genres.map { it.id }
            )

            val newFavoriteState = !_state.value.isFavorite
            _state.update { it.copy(isFavorite = newFavoriteState) }

            _effect.send(
                MovieDetailContract.Effect.ShowMessage(
                    if (newFavoriteState) "Added to favorites"
                    else "Removed from favorites"
                )
            )
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(MovieDetailContract.Effect.NavigateBack)
        }
    }
}