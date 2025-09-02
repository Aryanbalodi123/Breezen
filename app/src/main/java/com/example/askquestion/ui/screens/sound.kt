//package com.example.askquestion.ui.screens
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.*
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.*
//import androidx.navigation.NavController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import com.example.askquestion.theme.CustomTypography
//import com.example.askquestion.theme.AppColors
//
//
//enum class SoundCategory {
//    MY_AIDS, WHITE_NOISE, MUSIC, PRAYERS, NATURE, MEDITATION
//}
//
//// Intelligent sound grouping to prevent audio conflicts
//enum class SoundGroup {
//    AMBIENT_BASE,      // Foundational background sounds
//    RHYTHMIC_ELEMENTS, // Sounds with distinct rhythm/beat
//    MUSICAL_HARMONY,   // Melodic and harmonic sounds
//    SPIRITUAL_FOCUS,   // Meditation and prayer sounds
//    NATURE_ATMOSPHERE, // Natural environmental sounds
//    TECHNICAL_NOISE    // Generated/artificial sounds
//}
//
//// Enhanced SoundItem with grouping and mixing properties
//@Stable
//data class SoundItem(
//    val name: String,
//    val icon: ImageVector,
//    val category: SoundCategory,
//    val duration: String = "∞",
//    val soundGroup: SoundGroup,
//    val mixingPriority: Int = 1, // 1-3: how dominant this sound should be
//    val conflictsWith: List<SoundGroup> = emptyList()
//)
//
//// Mixing rules and compatibility
//object SoundMixingRules {
//
//    // Maximum sounds allowed per group simultaneously
//    val groupLimits = mapOf(
//        SoundGroup.RHYTHMIC_ELEMENTS to 1,  // Only one rhythmic sound
//        SoundGroup.MUSICAL_HARMONY to 2,    // Max 2 musical pieces
//        SoundGroup.SPIRITUAL_FOCUS to 2,    // Max 2 spiritual sounds
//        SoundGroup.AMBIENT_BASE to 3,       // Max 3 ambient sounds
//        SoundGroup.NATURE_ATMOSPHERE to 3,  // Max 3 nature sounds
//        SoundGroup.TECHNICAL_NOISE to 1     // Only one technical noise
//    )
//
//    // Groups that conflict with each other
//    val conflictingGroups = mapOf(
//        SoundGroup.RHYTHMIC_ELEMENTS to listOf(SoundGroup.SPIRITUAL_FOCUS),
//        SoundGroup.TECHNICAL_NOISE to listOf(SoundGroup.MUSICAL_HARMONY, SoundGroup.SPIRITUAL_FOCUS)
//    )
//
//    // Recommended combinations (these work well together)
//    val harmonicCombinations = listOf(
//        setOf(SoundGroup.AMBIENT_BASE, SoundGroup.MUSICAL_HARMONY),
//        setOf(SoundGroup.NATURE_ATMOSPHERE, SoundGroup.SPIRITUAL_FOCUS),
//        setOf(SoundGroup.AMBIENT_BASE, SoundGroup.NATURE_ATMOSPHERE),
//        setOf(SoundGroup.MUSICAL_HARMONY, SoundGroup.SPIRITUAL_FOCUS)
//    )
//
//    // Check if adding a sound would violate mixing rules
//    fun canAddSound(newSound: SoundItem, currentSelection: List<SoundItem>): MixingResult {
//        val currentGroups = currentSelection.groupBy { it.soundGroup }
//
//        // Check group limits
//        val newGroupCount = currentGroups[newSound.soundGroup]?.size ?: 0
//        val maxAllowed = groupLimits[newSound.soundGroup] ?: Int.MAX_VALUE
//
//        if (newGroupCount >= maxAllowed) {
//            return MixingResult.GroupLimitExceeded(newSound.soundGroup, maxAllowed)
//        }
//
//        // Check conflicts
//        val conflictingGroupsForNew = conflictingGroups[newSound.soundGroup] ?: emptyList()
//        val hasConflictingSound = currentSelection.any { existing ->
//            conflictingGroupsForNew.contains(existing.soundGroup) ||
//                    existing.conflictsWith.contains(newSound.soundGroup)
//        }
//
//        if (hasConflictingSound) {
//            val conflictingSounds = currentSelection.filter { existing ->
//                conflictingGroupsForNew.contains(existing.soundGroup) ||
//                        existing.conflictsWith.contains(newSound.soundGroup)
//            }
//            return MixingResult.ConflictDetected(conflictingSounds)
//        }
//
//        return MixingResult.CanAdd
//    }
//
//    // Get suggested sounds based on current selection
//    fun getSuggestedSounds(currentSelection: List<SoundItem>, allSounds: List<SoundItem>): List<SoundItem> {
//        if (currentSelection.isEmpty()) return emptyList()
//
//        val currentGroups = currentSelection.map { it.soundGroup }.toSet()
//
//        return allSounds.filter { sound ->
//            // Don't suggest already selected sounds
//            !currentSelection.contains(sound) &&
//                    // Check if it would create a harmonic combination
//                    harmonicCombinations.any { combo ->
//                        combo.contains(sound.soundGroup) && combo.intersect(currentGroups).isNotEmpty()
//                    } &&
//                    // Make sure it can actually be added
//                    canAddSound(sound, currentSelection) is MixingResult.CanAdd
//        }.take(6) // Limit suggestions
//    }
//}
//
//sealed class MixingResult {
//    object CanAdd : MixingResult()
//    data class GroupLimitExceeded(val group: SoundGroup, val maxAllowed: Int) : MixingResult()
//    data class ConflictDetected(val conflictingSounds: List<SoundItem>) : MixingResult()
//}
//
//@Composable
//fun SoundsScreen(navController: NavController) {
//    val selectedItems = remember { mutableStateListOf<SoundItem>() }
//    var activeCategory by remember { mutableStateOf(SoundCategory.WHITE_NOISE) }
//    var showMixingWarning by remember { mutableStateOf<String?>(null) }
//
//    val animatedAlpha by animateFloatAsState(
//        targetValue = if (selectedItems.isNotEmpty()) 1f else 0f,
//        animationSpec = tween(300), label = ""
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        AppColors.DarkBackground,
//                        Color(0xFF0F0F0F),
//                        AppColors.SurfaceBackground
//                    )
//                )
//            )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 100.dp)
//        ) {
//            // Premium Header
//            PremiumHeader()
//
//            // Enhanced Sound Scenes
//            PremiumSoundScenes()
//
//            // Category Tabs
//            PremiumCategoryTabs(
//                activeCategory = activeCategory,
//                onCategoryChange = { activeCategory = it }
//            )
//
//            // Sound Grid with intelligent mixing
//            PremiumSoundGrid(
//                category = activeCategory,
//                selectedItems = selectedItems,
//                onItemToggle = { sound ->
//                    if (selectedItems.contains(sound)) {
//                        selectedItems.remove(sound)
//                        showMixingWarning = null
//                    } else {
//                        val mixingResult = SoundMixingRules.canAddSound(sound, selectedItems.toList())
//                        when (mixingResult) {
//                            is MixingResult.CanAdd -> {
//                                selectedItems.add(sound)
//                                showMixingWarning = null
//                            }
//                            is MixingResult.GroupLimitExceeded -> {
//                                showMixingWarning = "Maximum ${mixingResult.maxAllowed} ${getGroupName(mixingResult.group)} sounds allowed"
//                            }
//                            is MixingResult.ConflictDetected -> {
//                                showMixingWarning = "Cannot mix with ${mixingResult.conflictingSounds.first().name}. These sounds don't blend well together."
//                            }
//                        }
//                    }
//                }
//            )
//
//            // Show suggestions if sounds are selected
//            if (selectedItems.isNotEmpty()) {
//                SuggestedSounds(
//                    currentSelection = selectedItems.toList(),
//                    onSoundSelect = { sound ->
//                        selectedItems.add(sound)
//                    }
//                )
//            }
//        }
//
//        // Floating Mix Player
//        if (selectedItems.isNotEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(horizontal = 20.dp, vertical = 100.dp)
//                    .graphicsLayer { alpha = animatedAlpha }
//            ) {
//                PremiumMixPlayer(selectedItems.map { it.name })
//            }
//        }
//
//        // Mixing Warning Snackbar
//        showMixingWarning?.let { warning ->
//            LaunchedEffect(warning) {
//                kotlinx.coroutines.delay(3000)
//                showMixingWarning = null
//            }
//
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    .padding(20.dp)
//            ) {
//                MixingWarningCard(warning)
//            }
//        }
//    }
//}
//
//@Composable
//fun SuggestedSounds(
//    currentSelection: List<SoundItem>,
//    onSoundSelect: (SoundItem) -> Unit
//) {
//    val allSounds = remember { getAllSounds() }
//    val suggestions = remember(currentSelection) {
//        SoundMixingRules.getSuggestedSounds(currentSelection, allSounds)
//    }
//
//    if (suggestions.isNotEmpty()) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 16.dp)
//        ) {
//            Text(
//                text = "Suggested Combinations",
//                style = CustomTypography.titleMedium,
//                color = AppColors.TextPrimary
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .horizontalScroll(rememberScrollState()),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                suggestions.forEach { suggestion ->
//                    SuggestionCard(
//                        sound = suggestion,
//                        onClick = { onSoundSelect(suggestion) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SuggestionCard(
//    sound: SoundItem,
//    onClick: () -> Unit
//) {
//    val categoryColor = getCategoryColor(sound.category)
//
//    Box(
//        modifier = Modifier
//            .width(100.dp)
//            .height(60.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .background(categoryColor.copy(alpha = 0.1f))
//            .border(
//                width = 1.dp,
//                color = categoryColor.copy(alpha = 0.3f),
//                shape = RoundedCornerShape(12.dp)
//            )
//            .clickable { onClick() }
//            .padding(8.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Icon(
//                imageVector = sound.icon,
//                contentDescription = null,
//                tint = categoryColor,
//                modifier = Modifier.size(16.dp)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = sound.name,
//                style = CustomTypography.bodySmall.copy(fontSize = 9.sp),
//                color = AppColors.TextPrimary,
//                maxLines = 1
//            )
//        }
//    }
//}
//
//@Composable
//fun MixingWarningCard(warning: String) {
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(16.dp))
//            .background(Color(0xFFFF6B6B).copy(alpha = 0.9f))
//            .padding(16.dp)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.Default.Warning,
//                contentDescription = null,
//                tint = Color.White,
//                modifier = Modifier.size(20.dp)
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Text(
//                text = warning,
//                style = CustomTypography.bodyMedium,
//                color = Color.White
//            )
//        }
//    }
//}
//
//@Composable
//fun PremiumHeader() {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp, vertical = 24.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "Sounds",
//            style = CustomTypography.headlineLarge,
//            color = AppColors.TextPrimary
//        )
//
//        GlassIconButton(
//            icon = Icons.Default.Search,
//            onClick = { /* Handle search */ }
//        )
//    }
//}
//
//@Composable
//fun GlassIconButton(
//    icon: ImageVector,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .size(48.dp)
//            .clip(RoundedCornerShape(16.dp))
//            .background(AppColors.GlassBackground)
//            .border(
//                width = 1.dp,
//                color = AppColors.GlassBorder,
//                shape = RoundedCornerShape(16.dp)
//            )
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = AppColors.TextPrimary,
//            modifier = Modifier.size(20.dp)
//        )
//    }
//}
//
//@Composable
//fun PremiumSoundScenes() {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp, vertical = 16.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Featured Scenes",
//                style = CustomTypography.titleLarge,
//                color = AppColors.TextPrimary
//            )
//            Text(
//                text = "View All",
//                style = CustomTypography.bodyMedium,
//                color = AppColors.PrimaryGreen,
//                modifier = Modifier.clickable { /* Handle view all */ }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .horizontalScroll(rememberScrollState()),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            PremiumSceneCard(
//                title = "Ocean Waves",
//                subtitle = "Deep sleep sounds",
//                icon = Icons.Default.Waves,
//                category = SoundCategory.NATURE
//            )
//
//            PremiumSceneCard(
//                title = "Forest Rain",
//                subtitle = "Nature & rainfall",
//                icon = Icons.Default.Forest,
//                category = SoundCategory.NATURE
//            )
//
//            PremiumSceneCard(
//                title = "Meditation",
//                subtitle = "Inner peace",
//                icon = Icons.Default.SelfImprovement,
//                category = SoundCategory.MEDITATION
//            )
//
//            PremiumSceneCard(
//                title = "Focus Mode",
//                subtitle = "White noise blend",
//                icon = Icons.Default.CenterFocusStrong,
//                category = SoundCategory.WHITE_NOISE
//            )
//        }
//    }
//}
//
//@Composable
//fun PremiumSceneCard(
//    title: String,
//    subtitle: String,
//    icon: ImageVector,
//    category: SoundCategory
//) {
//    val categoryColor = getCategoryColor(category)
//
//    Box(
//        modifier = Modifier
//            .width(180.dp)
//            .height(140.dp)
//            .clip(RoundedCornerShape(24.dp))
//            .background(
//                brush = Brush.horizontalGradient(
//                    colors = listOf(
//                        categoryColor.copy(alpha = 0.15f),
//                        categoryColor.copy(alpha = 0.08f)
//                    )
//                )
//            )
//            .border(
//                width = 1.dp,
//                color = categoryColor.copy(alpha = 0.3f),
//                shape = RoundedCornerShape(24.dp)
//            )
//            .clickable { /* Handle scene selection */ }
//            .padding(20.dp)
//    ) {
//        // Content positioned to avoid overlap with play button
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(end = 40.dp) // Leave space for play button
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(categoryColor.copy(alpha = 0.2f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    tint = categoryColor,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Text(
//                text = title,
//                style = CustomTypography.bodyLarge,
//                color = AppColors.TextPrimary,
//                maxLines = 1
//            )
//
//            Text(
//                text = subtitle,
//                style = CustomTypography.bodySmall,
//                color = AppColors.TextSecondary,
//                maxLines = 1
//            )
//        }
//
//        // Play button positioned at bottom right
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .size(32.dp)
//                .clip(CircleShape)
//                .background(categoryColor.copy(alpha = 0.2f))
//                .clickable { /* Handle play */ },
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.PlayArrow,
//                contentDescription = null,
//                tint = categoryColor,
//                modifier = Modifier.size(16.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun PremiumCategoryTabs(
//    activeCategory: SoundCategory,
//    onCategoryChange: (SoundCategory) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp, vertical = 20.dp)
//            .horizontalScroll(rememberScrollState()),
//        horizontalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        SoundCategory.values().forEach { category ->
//            PremiumCategoryTab(
//                category = category,
//                isActive = activeCategory == category,
//                onClick = { onCategoryChange(category) }
//            )
//        }
//    }
//}
//
//@Composable
//fun PremiumCategoryTab(
//    category: SoundCategory,
//    isActive: Boolean,
//    onClick: () -> Unit
//) {
//    val categoryColor = getCategoryColor(category)
//    val animatedColor by animateColorAsState(
//        targetValue = if (isActive) categoryColor else AppColors.TextSecondary,
//        animationSpec = tween(300), label = ""
//    )
//
//    Column(
//        modifier = Modifier.clickable { onClick() },
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = getCategoryName(category),
//            style = if (isActive) CustomTypography.bodyLarge else CustomTypography.bodyMedium,
//            color = animatedColor
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Box(
//            modifier = Modifier
//                .width(if (isActive) 24.dp else 0.dp)
//                .height(3.dp)
//                .clip(RoundedCornerShape(2.dp))
//                .background(categoryColor)
//        )
//    }
//}
//
//@Composable
//fun PremiumSoundGrid(
//    category: SoundCategory,
//    selectedItems: List<SoundItem>,
//    onItemToggle: (SoundItem) -> Unit
//) {
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(4),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        contentPadding = PaddingValues(4.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        val sounds = getSoundsForCategory(category)
//        items(sounds.size) { index ->
//            val sound = sounds[index]
//            val isSelected = selectedItems.contains(sound)
//            val canAdd = if (!isSelected) {
//                SoundMixingRules.canAddSound(sound, selectedItems) is MixingResult.CanAdd
//            } else true
//
//            PremiumSoundCard(
//                sound = sound,
//                isSelected = isSelected,
//                canAdd = canAdd,
//                onClick = { onItemToggle(sound) }
//            )
//        }
//    }
//}
//
//@Composable
//fun PremiumSoundCard(
//    sound: SoundItem,
//    isSelected: Boolean,
//    canAdd: Boolean,
//    onClick: () -> Unit
//) {
//    val categoryColor = getCategoryColor(sound.category)
//    val animatedScale by animateFloatAsState(
//        targetValue = if (isSelected) 1.05f else 1f,
//        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
//    )
//
//    val cardColor = when {
//        isSelected -> categoryColor
//        canAdd -> AppColors.TextSecondary
//        else -> Color.Gray
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(80.dp)
//            .graphicsLayer {
//                scaleX = animatedScale
//                scaleY = animatedScale
//                alpha = if (canAdd || isSelected) 1f else 0.5f
//            }
//            .clip(RoundedCornerShape(16.dp))
//            .background(
//                if (isSelected) {
//                    Brush.verticalGradient(
//                        colors = listOf(
//                            categoryColor.copy(alpha = 0.3f),
//                            categoryColor.copy(alpha = 0.2f)
//                        )
//                    )
//                } else {
//                    Brush.verticalGradient(
//                        colors = listOf(
//                            AppColors.CardBackground.copy(alpha = 0.4f),
//                            AppColors.CardBackground.copy(alpha = 0.2f)
//                        )
//                    )
//                }
//            )
//            .border(
//                width = if (isSelected) 2.dp else 1.dp,
//                color = if (isSelected) categoryColor else AppColors.GlassBorder.copy(alpha = 0.5f),
//                shape = RoundedCornerShape(16.dp)
//            )
//            .clickable(enabled = canAdd || isSelected) { onClick() }
//            .padding(8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Icon(
//            imageVector = sound.icon,
//            contentDescription = null,
//            tint = cardColor,
//            modifier = Modifier.size(20.dp)
//        )
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = sound.name,
//            style = CustomTypography.bodySmall.copy(fontSize = 10.sp),
//            color = if (isSelected) categoryColor else cardColor,
//            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
//            maxLines = 1
//        )
//
//        if (sound.duration != "∞") {
//            Text(
//                text = sound.duration,
//                style = CustomTypography.bodySmall.copy(fontSize = 8.sp),
//                color = AppColors.TextTertiary,
//                maxLines = 1
//            )
//        }
//    }
//}
//
//@Composable
//fun PremiumMixPlayer(selectedItems: List<String>) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(24.dp))
//            .background(
//                brush = Brush.horizontalGradient(
//                    colors = listOf(
//                        AppColors.PrimaryGreen.copy(alpha = 0.9f),
//                        AppColors.LightGreen.copy(alpha = 0.8f)
//                    )
//                )
//            )
//            .border(
//                width = 1.dp,
//                color = AppColors.GlassBorder,
//                shape = RoundedCornerShape(24.dp)
//            )
//            .padding(20.dp)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(56.dp)
//                    .clip(RoundedCornerShape(16.dp))
//                    .background(Color.White.copy(alpha = 0.2f))
//                    .clickable { /* Handle play/pause */ },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.PlayArrow,
//                    contentDescription = null,
//                    tint = Color.White,
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Custom Mix",
//                    style = CustomTypography.bodyLarge,
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Text(
//                    text = "${selectedItems.size} sounds • Playing",
//                    style = CustomTypography.bodySmall,
//                    color = Color.White.copy(alpha = 0.9f)
//                )
//            }
//
//            Column(horizontalAlignment = Alignment.End) {
//                Text(
//                    text = "∞",
//                    style = CustomTypography.bodyLarge,
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Text(
//                    text = "Duration",
//                    style = CustomTypography.bodySmall,
//                    color = Color.White.copy(alpha = 0.8f)
//                )
//            }
//        }
//    }
//}
//
//// Helper Functions
//fun getCategoryColor(category: SoundCategory): Color {
//    return when (category) {
//        SoundCategory.MY_AIDS -> Color(0xFF4CAF50)
//        SoundCategory.WHITE_NOISE -> Color(0xFF9C27B0)
//        SoundCategory.MUSIC -> Color(0xFFFF9800)
//        SoundCategory.PRAYERS -> Color(0xFF2196F3)
//        SoundCategory.NATURE -> Color(0xFF4CAF50)
//        SoundCategory.MEDITATION -> Color(0xFF673AB7)
//    }
//}
//
//fun getCategoryName(category: SoundCategory): String {
//    return when (category) {
//        SoundCategory.MY_AIDS -> "My Library"
//        SoundCategory.WHITE_NOISE -> "White Noise"
//        SoundCategory.MUSIC -> "Music"
//        SoundCategory.PRAYERS -> "Prayers"
//        SoundCategory.NATURE -> "Nature"
//        SoundCategory.MEDITATION -> "Meditation"
//    }
//}
//
//fun getGroupName(group: SoundGroup): String {
//    return when (group) {
//        SoundGroup.AMBIENT_BASE -> "ambient"
//        SoundGroup.RHYTHMIC_ELEMENTS -> "rhythmic"
//        SoundGroup.MUSICAL_HARMONY -> "musical"
//        SoundGroup.SPIRITUAL_FOCUS -> "spiritual"
//        SoundGroup.NATURE_ATMOSPHERE -> "nature"
//        SoundGroup.TECHNICAL_NOISE -> "technical"
//    }
//}
//
//fun getAllSounds(): List<SoundItem> {
//    return SoundCategory.values().flatMap { getSoundsForCategory(it) }
//}
//
//fun getSoundsForCategory(category: SoundCategory): List<SoundItem> {
//    return when (category) {
//        SoundCategory.MY_AIDS -> listOf(
//            SoundItem("Favorites", Icons.Default.Favorite, category,
//                soundGroup = SoundGroup.AMBIENT_BASE),
//            SoundItem("Recent", Icons.Default.History, category,
//                soundGroup = SoundGroup.AMBIENT_BASE),
//            SoundItem("Downloaded", Icons.Default.Download, category,
//                soundGroup = SoundGroup.AMBIENT_BASE),
//            SoundItem("Custom Mix", Icons.Default.QueueMusic, category,
//                soundGroup = SoundGroup.MUSICAL_HARMONY),
//            SoundItem("Sleep Timer", Icons.Default.Timer, category,
//                soundGroup = SoundGroup.AMBIENT_BASE),
//            SoundItem("My Sounds", Icons.Default.LibraryMusic, category,
//                soundGroup = SoundGroup.MUSICAL_HARMONY)
//        )
//
//        SoundCategory.WHITE_NOISE -> listOf(
//            SoundItem("Rain", Icons.Default.Cloud, category, "45:00",
//                SoundGroup.AMBIENT_BASE, mixingPriority = 2),
//            SoundItem("Campfire", Icons.Default.LocalFireDepartment, category,
//                soundGroup = SoundGroup.AMBIENT_BASE, mixingPriority = 2),
//            SoundItem("Clock Tick", Icons.Default.Schedule, category,
//                soundGroup = SoundGroup.RHYTHMIC_ELEMENTS, mixingPriority = 1,
//                conflictsWith = listOf(SoundGroup.SPIRITUAL_FOCUS, SoundGroup.MUSICAL_HARMONY)),
//            SoundItem("Keyboard", Icons.Default.Keyboard, category,
//                soundGroup = SoundGroup.RHYTHMIC_ELEMENTS, mixingPriority = 1,
//                conflictsWith = listOf(SoundGroup.SPIRITUAL_FOCUS, SoundGroup.MUSICAL_HARMONY)),
//            SoundItem("Wind", Icons.Default.Air, category,
//                soundGroup = SoundGroup.AMBIENT_BASE, mixingPriority = 2),
//            SoundItem("Fan", Icons.Default.Refresh, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 1),
//            SoundItem("Static", Icons.Default.GraphicEq, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 1),
//            SoundItem("Brown Noise", Icons.Default.Tune, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 2),
//            SoundItem("Pink Noise", Icons.Default.Equalizer, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 2),
//            SoundItem("White Noise", Icons.Default.LinearScale, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 2),
//            SoundItem("Vacuum", Icons.Default.CleaningServices, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 1),
//            SoundItem("Hair Dryer", Icons.Default.Dry, category,
//                soundGroup = SoundGroup.TECHNICAL_NOISE, mixingPriority = 1)
//        )
//
//        SoundCategory.MUSIC -> listOf(
//            SoundItem("Piano", Icons.Default.Piano, category, "3:45",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 3),
//            SoundItem("Guitar", Icons.Default.MusicNote, category, "4:20",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 3),
//            SoundItem("Flute", Icons.Default.MusicNote, category, "5:10",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 2),
//            SoundItem("Violin", Icons.Default.MusicNote, category, "4:35",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 3),
//            SoundItem("Harp", Icons.Default.MusicNote, category, "6:15",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 2),
//            SoundItem("Cello", Icons.Default.MusicNote, category, "5:40",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 2),
//            SoundItem("Ambient", Icons.Default.Album, category,
//                soundGroup = SoundGroup.AMBIENT_BASE, mixingPriority = 1),
//            SoundItem("Classical", Icons.Default.LibraryMusic, category, "8:30",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 3),
//            SoundItem("Jazz", Icons.Default.MusicNote, category, "6:45",
//                SoundGroup.MUSICAL_HARMONY, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.SPIRITUAL_FOCUS)),
//            SoundItem("Lo-Fi", Icons.Default.Radio, category,
//                soundGroup = SoundGroup.AMBIENT_BASE, mixingPriority = 2),
//            SoundItem("Chimes", Icons.Default.NotificationImportant, category,
//                soundGroup = SoundGroup.MUSICAL_HARMONY, mixingPriority = 1),
//            SoundItem("Singing Bowl", Icons.Default.MusicNote, category,
//                soundGroup = SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2)
//        )
//
//        SoundCategory.PRAYERS -> listOf(
//            SoundItem("Om Chanting", Icons.Default.SelfImprovement, category, "10:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE)),
//            SoundItem("Quran", Icons.Default.MenuBook, category, "15:30",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE)),
//            SoundItem("Bhajans", Icons.Default.MusicNote, category, "8:20",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS)),
//            SoundItem("Gregorian", Icons.Default.Church, category, "12:45",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE)),
//            SoundItem("Tibetan", Icons.Default.SelfImprovement, category, "20:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2),
//            SoundItem("Mantras", Icons.Default.Spa, category, "18:15",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS)),
//            SoundItem("Bell Sounds", Icons.Default.NotificationsActive, category,
//                soundGroup = SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 1),
//            SoundItem("Azan", Icons.Default.Campaign, category, "5:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE, SoundGroup.MUSICAL_HARMONY)),
//            SoundItem("Temple Bells", Icons.Default.Notifications, category,
//                soundGroup = SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 1),
//            SoundItem("Sanskrit", Icons.Default.RecordVoiceOver, category, "25:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE)),
//            SoundItem("Hymns", Icons.Default.MusicNote, category, "12:30",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2),
//            SoundItem("Kirtan", Icons.Default.Groups, category, "16:45",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS))
//        )
//
//        SoundCategory.NATURE -> listOf(
//            SoundItem("Ocean Waves", Icons.Default.Waves, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 3),
//            SoundItem("Forest", Icons.Default.Forest, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 2),
//            SoundItem("Birds", Icons.Default.MusicNote, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 2),
//            SoundItem("Thunder", Icons.Default.Thunderstorm, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 2,
//                conflictsWith = listOf(SoundGroup.SPIRITUAL_FOCUS)),
//            SoundItem("Creek", Icons.Default.Stream, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 2),
//            SoundItem("Crickets", Icons.Default.BugReport, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 1),
//            SoundItem("Waterfall", Icons.Default.Landscape, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 3),
//            SoundItem("Wind Chimes", Icons.Default.MusicNote, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 1),
//            SoundItem("Leaves", Icons.Default.Eco, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 1),
//            SoundItem("Bonfire", Icons.Default.Whatshot, category,
//                soundGroup = SoundGroup.AMBIENT_BASE, mixingPriority = 2),
//            SoundItem("Seagulls", Icons.Default.MusicNote, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 1),
//            SoundItem("Mountain Wind", Icons.Default.Air, category,
//                soundGroup = SoundGroup.NATURE_ATMOSPHERE, mixingPriority = 2)
//        )
//
//        SoundCategory.MEDITATION -> listOf(
//            SoundItem("Deep Breathing", Icons.Default.Air, category, "10:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2),
//            SoundItem("Guided", Icons.Default.RecordVoiceOver, category, "15:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE, SoundGroup.MUSICAL_HARMONY)),
//            SoundItem("Zen Garden", Icons.Default.Spa, category,
//                soundGroup = SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2),
//            SoundItem("Chakra", Icons.Default.SelfImprovement, category, "20:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 2),
//            SoundItem("Mindfulness", Icons.Default.Psychology, category, "12:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE)),
//            SoundItem("Body Scan", Icons.Default.Accessibility, category, "18:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE, SoundGroup.MUSICAL_HARMONY)),
//            SoundItem("Zen Bells", Icons.Default.NotificationImportant, category,
//                soundGroup = SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 1),
//            SoundItem("Binaural", Icons.Default.Headphones, category, "30:00",
//                SoundGroup.TECHNICAL_NOISE, mixingPriority = 2,
//                conflictsWith = listOf(SoundGroup.MUSICAL_HARMONY, SoundGroup.SPIRITUAL_FOCUS)),
//            SoundItem("432Hz", Icons.Default.Tune, category, "25:00",
//                SoundGroup.TECHNICAL_NOISE, mixingPriority = 2,
//                conflictsWith = listOf(SoundGroup.MUSICAL_HARMONY, SoundGroup.SPIRITUAL_FOCUS)),
//            SoundItem("Visualization", Icons.Default.Visibility, category, "22:00",
//                SoundGroup.SPIRITUAL_FOCUS, mixingPriority = 3,
//                conflictsWith = listOf(SoundGroup.RHYTHMIC_ELEMENTS, SoundGroup.TECHNICAL_NOISE, SoundGroup.MUSICAL_HARMONY)),
//            SoundItem("Alpha Waves", Icons.Default.GraphicEq, category, "40:00",
//                SoundGroup.TECHNICAL_NOISE, mixingPriority = 2,
//                conflictsWith = listOf(SoundGroup.MUSICAL_HARMONY, SoundGroup.SPIRITUAL_FOCUS)),
//            SoundItem("Silence", Icons.Default.VolumeOff, category,
//                soundGroup = SoundGroup.AMBIENT_BASE, mixingPriority = 1)
//        )
//    }
//}