package com.example.breezen.feature.music

import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.network.Category
import com.example.breezen.core.network.Song
import com.example.breezen.core.network.Tab
import com.example.breezen.core.ui.components.EmptyStateMessage
import com.example.breezen.core.ui.components.LoadingPillsIndicator
import com.example.breezen.feature.music.components.CategoryFilterChips
import com.example.breezen.feature.music.components.MusicItemsGrid
import com.example.breezen.feature.music.components.MusicScreenHeader
import com.example.breezen.feature.music.components.TabButtonRow

@Composable
fun MusicScreen(
    viewModel: TabViewModel,
    navController: NavController,
) {
    val tabs: List<Tab> by viewModel.tabs
    val categories: Map<String, List<Category>> by viewModel.categories
    val songs: Map<String, List<Song>> by viewModel.songs
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    var selectedCategoryIndex by rememberSaveable { mutableStateOf(0) }

    val currentTab = if (tabs.isNotEmpty()) {
        tabs.getOrNull(selectedTabIndex) ?: tabs.first()
    } else {
        null
    }
    val currentCategories: List<Category>? = categories[currentTab?.id]
    Log.d("Music data", tabs.toString())
    val isLoading = tabs.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // --- UI FIX: Base black background
    ) {
        // --- UI FIX: New "Black and Red" Aurora Gradient Background ---
        AuroraBackground(modifier = Modifier.matchParentSize())
        // --- END FIX ---

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingPillsIndicator(
                        pillColor = MaterialTheme.colorScheme.primary, // This will be red
                        pillCount = 4,
                        maxHeight = 32.dp,
                        minHeight = 8.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading music...",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                MusicScreenHeader(
                    onBackClick = {
                        navController.popBackStack()
                    })

                Spacer(modifier = Modifier.height(24.dp))

                TabButtonRow(
                    tabs = tabs, selectedIndex = selectedTabIndex, onTabSelected = {
                        selectedTabIndex = it
                        selectedCategoryIndex = 0
                    })


                if (!currentCategories.isNullOrEmpty()) {
                    CategoryFilterChips(
                        categories = currentCategories,
                        selectedIndex = selectedCategoryIndex,
                        onCategorySelected = {
                            selectedCategoryIndex = it
                        })

                    Spacer(modifier = Modifier.height(24.dp))

                    val categoryId = currentCategories.getOrNull(selectedCategoryIndex)?.id
                    val songsForCategory: List<Song>? = songs[categoryId]

                    if (!songsForCategory.isNullOrEmpty()) {
                        MusicItemsGrid(
                            items = songsForCategory,
                            viewModel = viewModel,
                            navController = navController
                        )
                    } else {
                        EmptyStateMessage("No songs in this category.")
                    }

                } else if (!isLoading) {
                    EmptyStateMessage("No categories found.")
                }
            }
        }
    }
}

// --- THIS IS THE FIX ---
// Updated with a new "Black and Red" color palette
@Composable
private fun AuroraBackground(modifier: Modifier = Modifier) {

    // Subtle mint palette (VERY soft)
    val glowSoft = Color(0xFFB9FFE8)      // light mint
    val glowDeep = Color(0xFF6EF2C5)      // deeper mint

    Canvas(modifier = modifier) {

        // Pure black base
        drawRect(color = Color.Black)

        // VERY LIGHT top-left glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowSoft.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * -0.3f, size.height * -0.2f),
                radius = size.width * 1.2f
            ),
            radius = size.width * 1.2f,
            center = Offset(size.width * -0.3f, size.height * -0.2f)
        )

        // Subtle right-side vertical glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowDeep.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 1.2f, size.height * 0.5f),
                radius = size.width * 1.0f
            ),
            radius = size.width * 1.0f,
            center = Offset(size.width * 1.2f, size.height * 0.5f)
        )

        // Slight bottom aura (barely visible)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowSoft.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.4f, size.height * 1.2f),
                radius = size.width * 0.9f
            ),
            radius = size.width * 0.9f,
            center = Offset(size.width * 0.4f, size.height * 1.2f)
        )
    }
}

private fun DrawScope.drawGradientBall(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}