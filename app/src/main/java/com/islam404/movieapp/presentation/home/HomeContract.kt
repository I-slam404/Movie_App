package com.islam404.movieapp.presentation.home

import com.islam404.movieapp.domain.model.Movie

object HomeContract {
    data class State(
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val isRefreshing: Boolean = false,
        val movies: List<Movie> = emptyList(),
        val searchQuery: String = "",
        val isSearching: Boolean = false,
        val error: String? = null,
        val selectedCategory: MovieCategory = MovieCategory.POPULAR,
        val currentPage: Int = 1,
        val canLoadMore: Boolean = true
    )

    enum class MovieCategory(val displayName: String) {
        POPULAR("Popular"),
        TOP_RATED("Top Rated"),
        NOW_PLAYING("Now Playing")
    }

    sealed class Event {
        data object LoadMovies : Event()
        data object LoadMoreMovies : Event()
        data class OnSearchQueryChange(val query: String) : Event()
        data object OnSearchSubmit : Event()
        data object ClearSearch : Event()
        data class OnCategoryChange(val category: MovieCategory) : Event()
        data class OnMovieClick(val movieId: Int) : Event()
        data object Retry : Event()
        data object Refresh : Event()
    }

    sealed class Effect {
        data class NavigateToDetail(val movieId: Int) : Effect()
        data class ShowError(val message: String) : Effect()
        data class ShowDataUpdated(val message: String) : Effect()
    }
}