package com.islam404.movieapp.presentation.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.islam404.movieapp.R
import com.islam404.movieapp.presentation.detail.components.*
import com.islam404.movieapp.util.Constants
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MovieDetailScreen(
    movieId: Int,
    navigator: DestinationsNavigator,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MovieDetailContract.Effect.NavigateBack -> {
                    navigator.navigateUp()
                }
                is MovieDetailContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MovieDetailContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.movie_details)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(MovieDetailContract.Event.NavigateBack)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    ShimmerMovieDetail()
                }
                state.error != null -> {
                    ErrorContent(
                        message = state.error ?: "Unknown error",
                        onRetry = { viewModel.onEvent(MovieDetailContract.Event.Retry) }
                    )
                }
                state.movieDetail != null -> {
                    val movie = state.movieDetail!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Backdrop Image
                        MovieBackdrop(
                            backdropPath = movie.backdropPath,
                            title = movie.title
                        )

                        // Movie Info
                        MovieInfo(
                            movie = movie,
                            modifier = Modifier.padding(16.dp)
                        )

                        // Genres
                        if (movie.genres.isNotEmpty()) {
                            GenreSection(
                                genres = movie.genres,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cast
                        if (movie.cast.isNotEmpty()) {
                            CastSection(
                                cast = movie.cast,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Additional Info
                        AdditionalInfo(
                            movie = movie,
                            modifier = Modifier.padding(16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "😞",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}