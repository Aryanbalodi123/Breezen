package com.example.breezen.feature.music

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.network.Category
import com.example.breezen.core.network.Song
import com.example.breezen.core.network.Tab
import com.example.breezen.core.ui.components.EmptyStateMessage
import com.example.breezen.core.ui.components.LoadingPillsIndicator
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.feature.music.components.CategoryFilterChips
import com.example.breezen.feature.music.components.MusicItemsGrid
import com.example.breezen.feature.music.components.MusicScreenHeader
import com.example.breezen.feature.music.components.TabButtonRow

@Composable
fun MusicScreen(
    viewModel: TabViewModel,
    navController: NavController,
) {
    // Data observation
    val tabs: List<Tab> by viewModel.tabs
    val categories: Map<String, List<Category>> by viewModel.categories
    val songs: Map<String, List<Song>> by viewModel.songs

    // UI State management
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }

    // Derived state for current view context
    val currentTab = if (tabs.isNotEmpty()) tabs.getOrNull(selectedTabIndex) ?: tabs.first() else null
    val currentCategories: List<Category>? = categories[currentTab?.id]
    val isLoading = tabs.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient background effect
        AuroraBackground(modifier = Modifier.matchParentSize())

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingPillsIndicator(
                        pillColor = MaterialTheme.colorScheme.primary,
                        pillCount = 4,
                        maxHeight = 32.dp,
                        minHeight = 8.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading music...",
                        style = AppTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Custom header with navigation
                MusicScreenHeader(navController)

                Spacer(modifier = Modifier.height(24.dp))

                // Primary navigation: Tabs
                TabButtonRow(
                    tabs = tabs,
                    selectedIndex = selectedTabIndex,
                    onTabSelected = {
                        selectedTabIndex = it
                        selectedCategoryIndex = 0 // Reset sub-navigation on tab switch
                    }
                )

                // Secondary navigation: Categories
                if (!currentCategories.isNullOrEmpty()) {
                    CategoryFilterChips(
                        categories = currentCategories,
                        selectedIndex = selectedCategoryIndex,
                        onCategorySelected = {
                            selectedCategoryIndex = it
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resolve current category ID for data lookup
                    val category_id = currentCategories.getOrNull(selectedCategoryIndex)?.id

                    // Retrieve songs and filter invalid entries
                    val songsForCategory: List<Song>? = songs[category_id]?.filter { !it.got_error }

                    if (!songsForCategory.isNullOrEmpty()) {
                        MusicItemsGrid(
                            items = songsForCategory,
                            viewModel = viewModel,
                            navController = navController
                        )
                    } else {
                        EmptyStateMessage("No songs in this category.")
                    }

                } else {
                    EmptyStateMessage("No categories found.")
                }
            }
        }
    }
}

/**
 * Renders a custom ambient background with radial gradients.
 */
@Composable
private fun AuroraBackground(modifier: Modifier = Modifier) {
    val glowSoft = Color(0xFFB9FFE8)
    val glowDeep = Color(0xFF6EF2C5)

    Canvas(modifier = modifier) {
        drawRect(color = Color.Black)

        // Upper-left soft glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowSoft.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * -0.3f, size.height * -0.2f),
                radius = size.width * 1.2f
            ),
            radius = size.width * 1.2f,
            center = Offset(size.width * -0.3f, size.height * -0.2f)
        )

        // Lower-right accent glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowDeep.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 1.2f, size.height * 0.5f),
                radius = size.width * 1.0f
            ),
            radius = size.width * 1.0f,
            center = Offset(size.width * 1.2f, size.height * 0.5f)
        )
    }
}