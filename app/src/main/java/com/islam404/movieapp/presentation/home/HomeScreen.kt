package com.islam404.movieapp.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.islam404.movieapp.presentation.common.components.EmptyContent
import com.islam404.movieapp.presentation.common.components.ErrorContent
import com.islam404.movieapp.presentation.common.components.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.platform.LocalFocusManager
import com.ramcosta.composedestinations.generated.destinations.MovieDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.islam404.movieapp.presentation.home.components.*
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
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null &&
                    lastVisibleItem.index >= state.movies.size - 4 &&
                    !state.isLoadingMore &&
                    state.canLoadMore &&
                    !state.isLoading
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
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                    // Initial Loading with Shimmer
                    state.isLoading && state.movies.isEmpty() -> {
                        ShimmerMovieGrid()
                    }
                    // Error State
                    state.error != null && state.movies.isEmpty() -> {
                        ErrorContent(
                            message = state.error ?: "Unknown error",
                            onRetry = { viewModel.onEvent(HomeContract.Event.Retry) }
                        )
                    }
                    // Empty State
                    state.movies.isEmpty() -> {
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
                                key = { movie ->
                                    "${movie.id}_${state.movies.indexOf(movie)}"
                                }
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

private enum class ContentState {
    Loading, Error, Empty, Success
}