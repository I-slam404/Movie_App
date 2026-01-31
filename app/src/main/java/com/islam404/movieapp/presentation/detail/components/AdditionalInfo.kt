package com.islam404.movieapp.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islam404.movieapp.domain.model.MovieDetail
import java.text.NumberFormat
import java.util.*

@Composable
fun AdditionalInfo(
    movie: MovieDetail,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Status",
                    value = movie.status
                )

                InfoRow(
                    label = "Release Date",
                    value = movie.releaseDate
                )

                InfoRow(
                    label = "Runtime",
                    value = "${movie.runtime} minutes"
                )

                if (movie.budget > 0) {
                    InfoRow(
                        label = "Budget",
                        value = formatCurrency(movie.budget)
                    )
                }

                if (movie.revenue > 0) {
                    InfoRow(
                        label = "Revenue",
                        value = formatCurrency(movie.revenue)
                    )
                }

                InfoRow(
                    label = "Votes",
                    value = "${movie.voteCount} votes"
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}