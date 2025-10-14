package com.example.askquestion.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.askquestion.R
import com.example.askquestion.network.Song
import com.example.askquestion.playGetMusicFile
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography
import com.example.askquestion.theme.FunnelDisplayFamily
import com.example.askquestion.theme.gradientBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin


@Stable
@Composable
fun AppBackground(): Brush {
    return remember {
        Brush.verticalGradient(
            colors = listOf(
                AppColors.DarkBackground,
                Color(0xFF0F0F0F),
                Color(0xFF1A1A1C),
                AppColors.SurfaceBackground
            ), startY = 0f, endY = Float.POSITIVE_INFINITY
        )
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeContent(navController: NavController, viewModel: TabViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        if (viewModel.songs.value.isEmpty()) {
            viewModel.fetchSongData()
        }
    }

    val tabs by viewModel.tabs
    val songs by viewModel.songs
    val isLoading = tabs.isEmpty()

    LaunchedEffect(songs) {
        if (songs.isNotEmpty() && viewModel.headerSong == null) {
            val allSongs = songs.values.flatten()
            viewModel.headerSong = allSongs.randomOrNull()
            viewModel.featuredSongs = allSongs.shuffled().take(2)
        }
    }

    val headerSong = viewModel.headerSong
    val featuredSongs = viewModel.featuredSongs

    Log.d("Music data", tabs.toString())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // BUG FIX & FEATURE 1: Improved Animated Header
            AppHeader()

            Spacer(modifier = Modifier.height(28.dp))

            HeaderSection(headerSong, viewModel, navController, isLoading)

            Spacer(modifier = Modifier.height(28.dp))
            MoodSelector()


            Spacer(modifier = Modifier.height(28.dp))

            AffirmationSection()


            Spacer(modifier = Modifier.height(28.dp))

            FeaturedSection(navController, isLoading, viewModel, featuredSongs)

            Spacer(modifier = Modifier.height(28.dp))

            ChatBot()

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}


// FEATURE 1: Animated Breezen Header Text
@Composable
fun AppHeader() {
    var breezenText by remember { mutableStateOf("") }

    // Typewriter effect animation
    LaunchedEffect(Unit) {
        "Breezen".forEach { char ->
            breezenText += char
            delay(150)
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            breezenText, color = Color.White, style = CustomTypography.titleLarge.copy(
                fontFamily = FunnelDisplayFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                fontSize = 32.sp
            )
        )
        Spacer(Modifier.weight(1f))

        // FEATURE 2: 6-Sided Cookie Avatar Button
        Button(
            modifier = Modifier
                .size(48.dp)
                .clip(HexagonShape), // Apply custom hexagon shape
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, contentColor = Color.Black
            ),
            shape = HexagonShape, // Also set shape property for consistency
            onClick = { /* TODO: Navigate to user profile or settings */ }) {
            Icon(
                painter = painterResource(R.drawable.heart), // Changed icon
                contentDescription = "User Profile",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// FEATURE 2: Custom Hexagon Shape for the Avatar
object HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val radius = size.minDimension / 2f
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val angle = 2.0 * Math.PI / 6 // 6 sides

            moveTo(
                centerX + radius * cos(0.0).toFloat(),
                centerY + radius * sin(0.0).toFloat()
            )
            for (i in 1 until 6) {
                lineTo(
                    centerX + radius * cos(angle * i).toFloat(),
                    centerY + radius * sin(angle * i).toFloat()
                )
            }
            close()
        }
        return Outline.Generic(path)
    }
}

// FEATURE 3: Reusable Section Header for improved headings
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f)
        ),
        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
    )
}

@Composable
fun HeaderSection(song: Song?, viewModel: TabViewModel, navController: NavController, isLoading: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomEnd = 120.dp))
    ) {
        Row {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .gradientBackground(
                            listOf(
                                Color.Black, Color.Black, Color.Black,
                                Color(0xFF294577), Color(0xFF91658f), Color(0xFFc8b2c7)
                            ), angle = 45f
                        )
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "GOOD MORNING ARYAN",
                style = CustomTypography.bodySmall.copy(
                    letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            if (isLoading) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                )
            } else {
                Text(
                    text = song?.title ?: "No Song",
                    style = CustomTypography.displayMedium.copy(
                        fontWeight = FontWeight.Light, letterSpacing = 2.sp, fontSize = 48.sp
                    ),
                    color = Color.White
                )
            }

            if (isLoading) {
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                )
            } else {
                Text(
                    text = "${song?.duration?.div(60) ?: 0} MINUTES",
                    style = CustomTypography.bodySmall.copy(
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(24.dp))
            IconButton(
                onClick = {
                    if (!isLoading && song != null) {
                        playGetMusicFile(context, viewModel, song, coroutineScope, navController)
                    }
                },
                enabled = !isLoading && song != null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        if (isLoading) Color.White.copy(alpha = 0.3f) else Color.White
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    modifier = Modifier.size(22.dp),
                    contentDescription = "Play",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    ),
                    start = Offset(shimmer - 300f, 0f),
                    end = Offset(shimmer, 0f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    )
}

data class Affirmation(
    val id: Int,
    val text: String,
    @DrawableRes val backgroundResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffirmationSection() {
    // The list of affirmations now uses drawable resource IDs for backgrounds.
    val affirmations = remember {
        mutableStateListOf(
            Affirmation(1, "I am capable of achieving my goals", R.drawable.affirmation_card_01),
            Affirmation(2, "I choose to be happy and love myself today", R.drawable.affirmation_card_02),
            Affirmation(3, "My potential to succeed is infinite", R.drawable.affirmation_card_03),
            Affirmation(4, "I am resilient and can handle anything", R.drawable.affirmation_card_04),
            Affirmation(5, "I radiate positivity and attract good things", R.drawable.affirmation_card_05),
            Affirmation(6, "Today I choose joy and gratitude", R.drawable.affirmation_card_06),
            Affirmation(7, "I am worthy of love and respect", R.drawable.affirmation_card_07),
            Affirmation(8, "I trust in my journey and timing", R.drawable.affirmation_card_08)
            // Add more affirmations up to 25 as needed
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Assuming SectionHeader is a composable you have defined elsewhere
        // SectionHeader(title = "Daily Affirmations")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (affirmations.isEmpty()) {
                Text(
                    text = "You've gone through all affirmations for today!",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                affirmations.forEachIndexed { index, affirmation ->
                    // Show only the top 3 cards in the stack
                    if (index >= affirmations.size - 3) {
                        val stackIndex = affirmations.size - 1 - index
                        val isTopCard = index == affirmations.size - 1

                        AffirmationCard(
                            affirmation = affirmation,
                            isTopCard = isTopCard,
                            modifier = Modifier
                                .offset(y = (stackIndex * 12).dp)
                                .graphicsLayer {
                                    scaleX = 1f - (stackIndex * 0.04f)
                                    scaleY = 1f - (stackIndex * 0.04f)
                                    alpha = 1f - (stackIndex * 0.2f)
                                }
                                .padding(horizontal = (stackIndex * 12).dp),
                            onSwipe = {
                                affirmations.remove(affirmation)
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffirmationCard(
    affirmation: Affirmation,
    isTopCard: Boolean,
    modifier: Modifier = Modifier,
    onSwipe: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val dismissThreshold = screenWidthPx * 0.4f

    val cardModifier = if (isTopCard) {
        modifier.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y)
                        rotation.snapTo((offsetX.value / screenWidthPx) * 20f)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        val shouldDismiss = kotlin.math.abs(offsetX.value) > dismissThreshold

                        if (shouldDismiss) {
                            val targetX = if (offsetX.value > 0) screenWidthPx * 1.5f else -screenWidthPx * 1.5f
                            launch {
                                offsetX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    targetValue = offsetY.value + 100f,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                )
                            }
                            launch {
                                rotation.animateTo(
                                    targetValue = if (offsetX.value > 0) 30f else -30f,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                )
                            }
                            kotlinx.coroutines.delay(300)
                            onSwipe()
                        } else {
                            // Animate back to center if not dismissed
                            launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                rotation.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }
    } else {
        modifier
    }

    Box(
        modifier = cardModifier
            .offset(
                x = with(LocalDensity.current) { offsetX.value.toDp() },
                y = with(LocalDensity.current) { offsetY.value.toDp() }
            )
            .graphicsLayer {
                rotationZ = rotation.value
            }
            .height(240.dp)
            .fillMaxWidth()
            .shadow(
                elevation = if (isTopCard) 12.dp else 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Use the PNG as the background for the card
        Image(
            painter = painterResource(id = affirmation.backgroundResId),
            contentDescription = "Affirmation card background", // Descriptive text for accessibility
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Use Crop to fill the bounds without distorting
        )

        // Center text with shadow, displayed on top of the image
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = affirmation.text,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoodSelector() {
    val moods = listOf(
        MoodData(Mood.Happy, R.drawable.mood_happy, "Happy", "I'm feeling happy"),
        MoodData(Mood.Sad, R.drawable.mood_sad, "Sad", "I'm feeling sad"),
        MoodData(Mood.Angry, R.drawable.mood_angry, "Angry", "I'm feeling furious")
    )

    var selectedMoodIndex by remember { mutableStateOf(0) } // Start with Happy
    val selectedMood = moods[selectedMoodIndex]
    val CloveShape = MaterialShapes.Clover8Leaf.toShape()

    // Slow rotation animation for clove border
    val infiniteTransition = rememberInfiniteTransition(label = "cloveRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloveRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // FEATURE 3: Using the SectionHeader
        SectionHeader("Tune Your Vibe")

        // Subtitle based on selected mood
        Text(
            text = selectedMood.subtitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.animateContentSize()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mood Image with Spinning Clove Border
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CloveShape)
        ) {
            // Static Image
            Crossfade(
                targetState = selectedMood,
                animationSpec = tween(400),
                label = "imageFade"
            ) { mood ->
                Image(
                    painter = painterResource(
                        id = mood.drawableId
                    ),
                    contentDescription = mood.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Rotating Clove Border
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CloveShape)
                    .border(
                        width = 3.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CloveShape
                    )
                    .graphicsLayer(rotationZ = rotation)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Horizontal Slider with Vertical Line Thumb
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                value = selectedMoodIndex.toFloat(),
                onValueChange = { newIndex ->
                    selectedMoodIndex = newIndex.toInt()
                },
                valueRange = 0f..2f,
                steps = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thumb = {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                            .shadow(elevation = 4.dp)
                    )
                },
                track = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Left and Right Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = moods[0].label,
                    color = if (selectedMoodIndex == 0) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = if (selectedMoodIndex == 0) FontWeight.SemiBold else FontWeight.Normal
                )

                Text(
                    text = moods[2].label,
                    color = if (selectedMoodIndex == 2) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = if (selectedMoodIndex == 2) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// Data class for scalable mood management
data class MoodData(
    val mood: Mood,
    val drawableId: Int,
    val label: String,
    val subtitle: String
)

enum class Mood { Happy, Sad, Angry }

@Composable
fun FeaturedSection(
    navController: NavController,
    isLoading: Boolean,
    viewModel: TabViewModel,
    featuredSongs: List<Song>
) {
    Column {
        // FEATURE 3: Using the SectionHeader
        SectionHeader("Refreshing Tunes")

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureSectionCard1(
                    song = featuredSongs.getOrNull(0),
                    viewModel = viewModel,
                    navController = navController,
                    isLoading = isLoading
                )
            }
            item {
                FeatureSectionCard2(
                    song = featuredSongs.getOrNull(1),
                    viewModel = viewModel,
                    navController = navController,
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun FeatureSectionCard1(
    song: Song?,
    viewModel: TabViewModel,
    navController: NavController,
    isLoading: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF012f46), Color(0xFF00090e), Color.Black),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.gradient_circles),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                    )
                } else {
                    Text(
                        text = "${song?.duration?.div(60) ?: 0} min",
                        color = Color.White,
                        style = CustomTypography.titleSmall,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                } else {
                    Text(
                        song?.title ?: "No Song",
                        color = Color.White,
                        style = CustomTypography.bodyLarge.copy(
                            fontSize = 26.sp, fontWeight = FontWeight.Thin
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))

                IconButton(
                    onClick = {
                        if (!isLoading && song != null) {
                            playGetMusicFile(context, viewModel, song, coroutineScope, navController)
                        }
                    },
                    enabled = !isLoading && song != null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (isLoading) {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF012f46).copy(alpha = 0.5f),
                                        Color(0xFF07a796).copy(alpha = 0.5f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0xFF012f46), Color(0xFF07a796))
                                )
                            }
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Play",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureSectionCard2(
    song: Song?,
    viewModel: TabViewModel,
    navController: NavController,
    isLoading: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFdde46f),
                        Color(0xFF68a095),
                        Color(0xFF21366d),
                        Color(0xFF111333)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy((-50).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.End)
                    .offset(x = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(rotationY = 45f)
                )

                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer(rotationY = 45f)
                )

                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(190.dp)
                        .graphicsLayer(rotationY = 45f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                    )
                } else {
                    Text(
                        text = "${song?.duration?.div(60) ?: 0} min",
                        color = Color.White,
                        style = CustomTypography.titleSmall,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                } else {
                    Text(
                        song?.title ?: "No Song",
                        color = Color.White,
                        style = CustomTypography.bodyLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Thin
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))

                androidx.compose.material.IconButton(
                    onClick = {
                        if (!isLoading && song != null) {
                            playGetMusicFile(
                                context,
                                viewModel,
                                song,
                                coroutineScope,
                                navController
                            )
                            navController.navigate("player")
                        }
                    },
                    enabled = !isLoading && song != null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoading) {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFdde46f).copy(alpha = 0.5f),
                                        Color(0xFF68a095).copy(alpha = 0.5f),
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFdde46f),
                                        Color(0xFF68a095),
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Play",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChatBot() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3A9F8F).copy(alpha = 0.4f),
                            Color.Transparent
                        ), radius = 600f
                    )
                )
        )

        Text(
            text = "How are you feeling today?",
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )

        Image(
            painter = painterResource(R.drawable.chatbot_background),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(150.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f))
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var inputText by remember { mutableStateOf("") }

            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text("Type your thoughts...", color = Color.Gray)
                    }
                    innerTextField()
                })

            IconButton(
                onClick = { /* handle send */ },
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3A9F8F), Color(0xFF66E6C9))
                        )
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    modifier = Modifier.size(22.dp),
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}
