package com.islam404.movieapp.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.islam404.movieapp.R
import com.islam404.movieapp.presentation.detail.components.AdditionalInfo
import com.islam404.movieapp.presentation.detail.components.CastSection
import com.islam404.movieapp.presentation.detail.components.GenreSection
import com.islam404.movieapp.presentation.detail.components.MovieBackdrop
import com.islam404.movieapp.presentation.detail.components.MovieInfo
import com.islam404.movieapp.presentation.detail.components.ShimmerMovieDetail
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.movie_details)) }, navigationIcon = {
            IconButton(
                onClick = {
                    viewModel.onEvent(MovieDetailContract.Event.NavigateBack)
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
        )
    }, snackbarHost = {
        SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }) { padding ->
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
                        onRetry = { viewModel.onEvent(MovieDetailContract.Event.Retry) })
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
                            backdropPath = movie.backdropPath, title = movie.title
                        )

                        // Movie Info
                        MovieInfo(
                            movie = movie, modifier = Modifier.padding(16.dp)
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
                                cast = movie.cast, modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Additional Info
                        AdditionalInfo(
                            movie = movie, modifier = Modifier.padding(16.dp)
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
    message: String, onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "😞", style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRetry, shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}