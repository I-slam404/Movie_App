package com.islam404.movieapp.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.islam404.movieapp.util.Constants

@Composable
fun MovieBackdrop(
    backdropPath: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = "${Constants.IMAGE_BASE_URL}${Constants.BACKDROP_SIZE_W780}${backdropPath}",
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f)
                        ),
                        startY = 200f
                    )
                )
        )
    }
}