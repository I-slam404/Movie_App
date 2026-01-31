package com.islam404.movieapp.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.islam404.movieapp.ui.theme.MovieAppTheme
import com.islam404.movieapp.util.shimmerEffect

@Composable
fun ShimmerMovieCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Poster shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .shimmerEffect()
            )

            // Info shimmer
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Title shimmer (2 lines)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
fun ShimmerMovieGrid(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerMovieCard(modifier = Modifier.weight(1f))
                ShimmerMovieCard(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview
@Composable
private fun ShimmerMovieCardPreview() {
    MovieAppTheme {
        ShimmerMovieCard()
    }
}