package com.islam404.movieapp.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.islam404.movieapp.util.shimmerEffect

@Composable
fun ShimmerMovieDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Backdrop shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shimmerEffect()
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Title shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating info shimmer
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overview shimmer
            repeat(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 4) 0.7f else 1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cast shimmer
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
        }
    }
}