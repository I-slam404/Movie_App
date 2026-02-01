package com.islam404.movieapp.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.islam404.movieapp.presentation.home.HomeContract
import com.islam404.movieapp.ui.theme.MovieAppTheme

@Composable
fun CategoryTabs(
    selectedCategory: HomeContract.MovieCategory,
    onCategorySelected: (HomeContract.MovieCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedCategory.ordinal,
        modifier = modifier,
        edgePadding = 16.dp,
        indicator = { },
        divider = { },
        containerColor = Color.Transparent
    ) {
        HomeContract.MovieCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(300),
                label = "backgroundColor"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(300),
                label = "contentColor"
            )

            Surface(
                onClick = { onCategorySelected(category) },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = backgroundColor,
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoryTabsPreview() {
    MovieAppTheme {
        CategoryTabs(
            selectedCategory = HomeContract.MovieCategory.POPULAR,
            onCategorySelected = {}
        )
    }
}