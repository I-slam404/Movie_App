package com.islam404.movieapp.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.islam404.movieapp.presentation.common.components.EmptyContent
import com.islam404.movieapp.presentation.common.components.ErrorContent
import com.islam404.movieapp.presentation.home.components.CategoryTabs
import com.islam404.movieapp.presentation.home.components.MovieCard
import com.islam404.movieapp.presentation.home.components.SearchBar
import com.islam404.movieapp.presentation.home.components.ShimmerMovieCard
import com.islam404.movieapp.presentation.home.components.ShimmerMovieGrid
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.MovieDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyGridState()
    val focusManager = LocalFocusManager.current

    // Clear focus when scrolling starts
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    // Detect when user scrolls to bottom for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

            val shouldLoad = lastVisibleItem != null &&
                    lastVisibleItem.index >= state.movies.size - 4 &&
                    !state.isLoadingMore &&
                    state.canLoadMore &&
                    !state.isLoading &&
                    !state.isRefreshing

            shouldLoad
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.onEvent(HomeContract.Event.LoadMoreMovies)
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeContract.Effect.NavigateToDetail -> {
                    navigator.navigate(MovieDetailScreenDestination(movieId = effect.movieId))
                }
                is HomeContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is HomeContract.Effect.ShowDataUpdated -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedContent(
                            targetState = if (state.isSearching) "Search Results" else "Movies",
                            transitionSpec = {
                                fadeIn() + slideInVertically() togetherWith
                                        fadeOut() + slideOutVertically()
                            },
                            label = "title"
                        ) { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        // Show subtle indicator when refreshing in background
                        if (state.isRefreshing && state.movies.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = {
                    viewModel.onEvent(HomeContract.Event.OnSearchQueryChange(it))
                },
                onSearch = {
                    viewModel.onEvent(HomeContract.Event.OnSearchSubmit)
                },
                onClear = {
                    viewModel.onEvent(HomeContract.Event.ClearSearch)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Category Tabs (only visible when not searching)
            AnimatedVisibility(
                visible = !state.isSearching,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CategoryTabs(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.onEvent(HomeContract.Event.OnCategoryChange(category))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    // Initial Loading with Shimmer (only if no data)
                    state.isLoading && state.movies.isEmpty() -> {
                        ShimmerMovieGrid()
                    }
                    // Error State (only if no data)
                    state.error != null && state.movies.isEmpty() -> {
                        ErrorContent(
                            message = state.error ?: "Unknown error",
                            onRetry = { viewModel.onEvent(HomeContract.Event.Retry) }
                        )
                    }
                    // Empty State
                    state.movies.isEmpty() && !state.isLoading -> {
                        EmptyContent(isSearching = state.isSearching)
                    }
                    // Success State with Movies
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Movie Items
                            items(
                                items = state.movies,
                                key = { movie -> movie.id }
                            ) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = {
                                        viewModel.onEvent(
                                            HomeContract.Event.OnMovieClick(movie.id)
                                        )
                                    }
                                )
                            }

                            // Loading More Indicator (Shimmer at bottom)
                            if (state.isLoadingMore) {
                                items(2) {
                                    ShimmerMovieCard()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}