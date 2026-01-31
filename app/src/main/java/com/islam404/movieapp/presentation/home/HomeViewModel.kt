package com.islam404.movieapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islam404.movieapp.domain.usecase.GetPopularMoviesUseCase
import com.islam404.movieapp.domain.usecase.SearchMoviesUseCase
import com.islam404.movieapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.islam404.movieapp.domain.usecase.GetTopRatedMoviesUseCase
import com.islam404.movieapp.domain.usecase.GetNowPlayingMoviesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val searchMoviesUseCase: SearchMoviesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>(Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        onEvent(HomeContract.Event.LoadMovies)
    }

    fun onEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.LoadMovies -> loadMovies(page = 1, forceRefresh = false)
            is HomeContract.Event.LoadMoreMovies -> loadMoreMovies()
            is HomeContract.Event.OnSearchQueryChange -> handleSearchQueryChange(event.query)
            is HomeContract.Event.OnSearchSubmit -> {
                val query = _state.value.searchQuery.trim()
                if (query.isNotEmpty()) {
                    performSearch(query, page = 1)
                }
            }
            is HomeContract.Event.ClearSearch -> clearSearch()
            is HomeContract.Event.OnCategoryChange -> changeCategory(event.category)
            is HomeContract.Event.OnMovieClick -> navigateToDetail(event.movieId)
            is HomeContract.Event.Retry -> retry()
            is HomeContract.Event.Refresh -> refresh()
        }
    }

    private fun handleSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()

        if (query.trim().isEmpty()) {
            _state.update {
                it.copy(
                    isSearching = false,
                    movies = emptyList(),
                    currentPage = 1,
                    canLoadMore = true
                )
            }
            loadMovies(page = 1)
        } else {
            searchJob = viewModelScope.launch {
                delay(500)
                performSearch(query.trim(), page = 1)
            }
        }
    }

    private fun performSearch(query: String, page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }

            searchMoviesUseCase(query, page).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = page == 1,
                                isLoadingMore = page > 1,
                                error = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        val newMovies = result.data ?: emptyList()
                        _state.update { currentState ->
                            val updatedMovies = if (page == 1) {
                                newMovies
                            } else {
                                // Merge and remove duplicates by ID
                                (currentState.movies + newMovies).distinctBy { it.id }
                            }
                            currentState.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                isSearching = true,
                                movies = updatedMovies,
                                currentPage = page,
                                canLoadMore = newMovies.isNotEmpty(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = if (page == 1) result.message else null
                            )
                        }
                        _effect.send(
                            HomeContract.Effect.ShowError(
                                result.message ?: "Search failed"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadMovies(page: Int = 1, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val moviesFlow = when (_state.value.selectedCategory) {
                HomeContract.MovieCategory.POPULAR -> {
                    getPopularMoviesUseCase(page, forceRefresh)
                }
                HomeContract.MovieCategory.TOP_RATED -> {
                    getTopRatedMoviesUseCase(page, forceRefresh)
                }
                HomeContract.MovieCategory.NOW_PLAYING -> {
                    getNowPlayingMoviesUseCase(page, forceRefresh)
                }
            }

            moviesFlow.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = page == 1,
                                isLoadingMore = page > 1,
                                error = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        val newMovies = result.data ?: emptyList()
                        _state.update { currentState ->
                            val updatedMovies = if (page == 1) {
                                newMovies
                            } else {
                                // Merge and remove duplicates by ID
                                (currentState.movies + newMovies).distinctBy { it.id }
                            }
                            currentState.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                movies = updatedMovies,
                                currentPage = page,
                                canLoadMore = newMovies.isNotEmpty(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = if (page == 1) result.message else null
                            )
                        }
                        _effect.send(
                            HomeContract.Effect.ShowError(
                                result.message ?: "Unknown error"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadMoreMovies() {
        val currentState = _state.value
        if (currentState.isLoadingMore || !currentState.canLoadMore) {
            return
        }

        if (currentState.isSearching && currentState.searchQuery.isNotEmpty()) {
            performSearch(currentState.searchQuery.trim(), page = currentState.currentPage + 1)
        } else {
            loadMovies(page = currentState.currentPage + 1)
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()

        _state.update {
            it.copy(
                searchQuery = "",
                isSearching = false,
                movies = emptyList(),
                currentPage = 1,
                canLoadMore = true
            )
        }
        loadMovies(page = 1)
    }

    private fun changeCategory(category: HomeContract.MovieCategory) {
        searchJob?.cancel()

        _state.update {
            it.copy(
                selectedCategory = category,
                searchQuery = "",
                isSearching = false,
                movies = emptyList(),
                currentPage = 1,
                canLoadMore = true
            )
        }
        loadMovies(page = 1)
    }

    private fun retry() {
        if (_state.value.isSearching && _state.value.searchQuery.isNotEmpty()) {
            performSearch(_state.value.searchQuery.trim(), page = 1)
        } else {
            loadMovies(page = 1)
        }
    }

    private fun refresh() {
        searchJob?.cancel()

        _state.update {
            it.copy(
                searchQuery = "",
                isSearching = false,
                movies = emptyList(),
                currentPage = 1,
                canLoadMore = true
            )
        }
        loadMovies(page = 1, forceRefresh = true)
    }

    private fun navigateToDetail(movieId: Int) {
        viewModelScope.launch {
            _effect.send(HomeContract.Effect.NavigateToDetail(movieId))
        }
    }
}