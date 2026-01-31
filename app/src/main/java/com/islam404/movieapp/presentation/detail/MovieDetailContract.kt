package com.islam404.movieapp.presentation.detail

import com.islam404.movieapp.domain.model.MovieDetail

object MovieDetailContract {
    // State
    data class State(
        val isLoading: Boolean = false,
        val movieDetail: MovieDetail? = null,
        val isFavorite: Boolean = false,
        val error: String? = null
    )

    // Events
    sealed class Event {
        data class LoadMovieDetail(val movieId: Int) : Event()
        data object ToggleFavorite : Event()
        data object NavigateBack : Event()
        data object Retry : Event()
    }

    // Effects
    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowError(val message: String) : Effect()
        data class ShowMessage(val message: String) : Effect()
    }
}