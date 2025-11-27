package com.example.breezen.feature.music

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.core.network.Category
import com.example.breezen.core.network.Song
import com.example.breezen.core.network.Tab
import com.example.breezen.core.ui.components.BackButton
import com.example.breezen.core.ui.components.EmptyStateMessage
import com.example.breezen.core.ui.components.LoadingPillsIndicator
import com.example.breezen.feature.music.components.CategoryFilterChips
import com.example.breezen.feature.music.components.MusicItemsGrid
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

    val currentTab = if (tabs.isNotEmpty()) tabs.getOrNull(selectedTabIndex) ?: tabs.first() else null
    val currentCategories: List<Category>? = categories[currentTab?.id]
    val isLoading = tabs.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AuroraBackground(modifier = Modifier.matchParentSize())

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
                    Text("Loading music...", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // --- HEADER FIX: Inline implementation with Back button strictly on Left ---
                CustomMusicHeader(navController)

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

                    // --- SPACING FIX: Reduced from 24.dp to 12.dp ---
                    Spacer(modifier = Modifier.height(12.dp))

                    val categoryId = currentCategories.getOrNull(selectedCategoryIndex)?.id

                    // Filter out errors client side just in case
                    val songsForCategory: List<Song>? = songs[categoryId]?.filter { !it.got_error }

                    if (!songsForCategory.isNullOrEmpty()) {
                        // The Grid logic in MusicGrid.kt handles the "Sequential Loading"
                        // and implicit scroll reset via LaunchedEffect(items)
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

// --- CUSTOM HEADER COMPONENT ---
@Composable
private fun CustomMusicHeader(
    navController: NavController,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Left: Back button
        BackButton(navController = navController)

        // Center: Title
        Text(
            text =" Music Library",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


// --- Aurora Background (Unchanged) ---
@Composable
private fun AuroraBackground(modifier: Modifier = Modifier) {
    val glowSoft = Color(0xFFB9FFE8)
    val glowDeep = Color(0xFF6EF2C5)
    Canvas(modifier = modifier) {
        drawRect(color = Color.Black)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowSoft.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * -0.3f, size.height * -0.2f),
                radius = size.width * 1.2f
            ),
            radius = size.width * 1.2f,
            center = Offset(size.width * -0.3f, size.height * -0.2f)
        )
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