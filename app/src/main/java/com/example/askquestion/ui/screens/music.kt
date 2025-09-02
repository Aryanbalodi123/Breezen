package com.example.askquestion.ui.screens

import android.content.Context
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.askquestion.network.Category
import com.example.askquestion.network.RetroFitClient
import com.example.askquestion.network.SUPABASE_API_KEY_ANON
import com.example.askquestion.network.Tab
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.askquestion.R
import com.example.askquestion.network.IMAGE_BUCKET_URL
import com.example.askquestion.network.Song
import com.example.askquestion.network.TELEGRAM_BOT_TOKEN
import com.example.askquestion.network.retrieveMusicFile
import kotlinx.coroutines.coroutineScope



class TabViewModel : ViewModel(){

    enum class RepeatMode {
        OFF,
        ALL,
        ONE
    }

    var currentSong by mutableStateOf<Song?>(null)
    var filePath by mutableStateOf("")
    private val _tabs = mutableStateOf<List<Tab>>(emptyList())
    val tabs: State<List<Tab>> =_tabs
    private val _categories = mutableStateOf<Map<String, List<Category>>>(emptyMap())
    val categories: State<Map<String, List<Category>>> = _categories

    private val _songs = mutableStateOf<Map<String,List<Song>>>(emptyMap())
    val songs: State<Map<String, List<Song>>> = _songs

    val songCache = mutableMapOf<String , List<Song>>()

    var isPlaying by mutableStateOf(false)
    var isShuffleEnabled by mutableStateOf(false)
    var repeatMode: RepeatMode by mutableStateOf(RepeatMode.OFF)

    fun fetchTabsCategory(){
        viewModelScope.launch {
            val resultTab = RetroFitClient.api.getTabs(
                apiKey = SUPABASE_API_KEY_ANON,
                authorization = "Bearer $SUPABASE_API_KEY_ANON"
            )
            _tabs.value= resultTab

            val resultCategory = RetroFitClient.api.getCategories(
                apiKey = SUPABASE_API_KEY_ANON,
                authorization = "Bearer $SUPABASE_API_KEY_ANON"
            )
            _categories.value = resultCategory.groupBy{it.tab_id} // It will group all the categories to their tab id

            Log.d("TabsViewModel", "Fetched category: $resultCategory")

            val resultSong = RetroFitClient.api.getSongs(
                apiKey = SUPABASE_API_KEY_ANON,
                authorization = "Bearer $SUPABASE_API_KEY_ANON"
            )
            _songs.value = resultSong.groupBy{it.category_id} // It will group all the categories to their category id

            Log.d("TabsViewModel", "Fetched song: $resultSong")
        }
    }
    fun setCurrentSong(context:Context, song: Song , musicFilePath:String){
        currentSong = song
        Log.d("Song set" , song.toString())
        filePath = musicFilePath
        Log.d("file path set" , filePath)

    }


}

@Composable
fun MusicScreen(viewModel: TabViewModel = viewModel(),
    navController: NavController,

) {
    val tabs by viewModel.tabs
    val categories by viewModel.categories
    val songs by viewModel.songs
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    var selectedCategoryIndex by rememberSaveable { mutableStateOf(0) }


    LaunchedEffect (Unit){
        viewModel.fetchTabsCategory()
    }

    val currentTab = if (tabs.isNotEmpty()) {
        tabs.getOrNull(selectedTabIndex) ?: tabs.first()
    } else {
        null
    }
    val currentCategories = categories[currentTab?.id]

    LaunchedEffect(Unit) {
        delay(1000)
        isLoading = false
    }

    val backgroundGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                AppColors.DarkBackground,
                Color(0xFF0F0F0F),
                Color(0xFF1A1A1C),
                AppColors.SurfaceBackground
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
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
                        pillColor = AppColors.PrimaryGreen,
                        pillCount = 4,
                        maxHeight = 32.dp,
                        minHeight = 8.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Loading music...",
                        color = AppColors.TextSecondary,
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
                HeaderSection(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                TabButtonRow(
                    tabs = tabs,
                    selectedIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it
                  }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (currentCategories != null) {
                    CategoryFilterChips(
                        categories = currentCategories,
                        selectedIndex = selectedCategoryIndex,
                        onCategorySelected = { selectedCategoryIndex = it
                     }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (currentCategories != null ){
                    
                            if (songs.isNotEmpty() ) {
                                songs[currentCategories[selectedCategoryIndex].id]?.let {
                                    MusicItemsGrid(
                                        items = it,
                                        navController = navController,
                                        viewModel = viewModel
                                    )


                                }
                            }

                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = AppColors.TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingPillsIndicator(
    modifier: Modifier = Modifier,
    pillColor: Color = Color(0xFF4CAF50),
    pillCount: Int = 4,
    animationDuration: Int = 600,
    minHeight: Dp = 12.dp,
    maxHeight: Dp = 40.dp,
    pillWidth: Dp = 8.dp,
    spacing: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pills")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pillCount) { index ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = minHeight.value,
                targetValue = maxHeight.value,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        offsetMillis = (animationDuration / pillCount) * index
                    )
                ),
                label = "pill_height_$index"
            )

            Box(
                modifier = Modifier
                    .width(pillWidth)
                    .height(animatedHeight.dp)
                    .background(
                        color = pillColor,
                        shape = RoundedCornerShape(pillWidth / 2)
                    )
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(pillWidth / 2),
                        ambientColor = pillColor.copy(alpha = 0.3f),
                        spotColor = pillColor.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
private fun HeaderSection(
    onBackClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "backButtonScale"
    )

    val headerAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, delayMillis = 100),
        label = "headerAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
            .graphicsLayer { alpha = headerAlpha },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF1A1A1A),
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .border(
                    width = 1.dp,
                    color = AppColors.PrimaryGreen.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) {
                    isPressed = true
                    onBackClick()
                }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.PrimaryGreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }

        Text(
            text = "Music Library",
            style = CustomTypography.titleLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.size(48.dp))
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun TabButtonRow(
    tabs: List<Tab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            TabItem(
                title = tab.name,
                isSelected = index == selectedIndex,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.PrimaryGreen else AppColors.TextSecondary,
        animationSpec = tween(300), label = "tabTextColor"
    )

    val underlineWidth by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 0.dp,
        animationSpec = tween(300), label = "underlineAnim"
    )

    Column(
        modifier = Modifier
            .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = animatedColor,
            style = if (isSelected) CustomTypography.bodyLarge else CustomTypography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .height(3.dp)
                .width(underlineWidth)
                .clip(RoundedCornerShape(2.dp))
                .background(AppColors.PrimaryGreen)
        )
    }
}

@Composable
private fun CategoryFilterChips(
    categories: List<Category>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            count = categories.size,
            key = { index -> categories[index].name }
        ) { index ->
            EnhancedCategoryFilterChip(
                text = categories[index].name,
                isSelected = selectedIndex == index,
                onClick = { onCategorySelected(index) }
            )
        }
    }
}

@Composable
private fun EnhancedCategoryFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "categoryChipScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color(0xFF0F3A2F)
        } else {
            Color(0xFF161616)
        },
        animationSpec = tween(300),
        label = "categoryChipBackground"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            AppColors.PrimaryGreen
        } else {
            AppColors.TextTertiary
        },
        animationSpec = tween(300),
        label = "categoryChipText"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            AppColors.PrimaryGreen.copy(alpha = 0.6f)
        } else {
            Color(0xFF2A2A2A)
        },
        animationSpec = tween(300),
        label = "categoryChipBorder"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier
            .scale(scale)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) {
                isPressed = true
                onClick()
            }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = CustomTypography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                fontSize = 13.sp
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun MusicItemsGrid(
    items: List<Song>,
    navController: NavController,
    viewModel: TabViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Track which items have loaded (in order of loading, not grid order)
    var loadedItems by remember { mutableStateOf(setOf<Int>()) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items) { index, musicItem ->
            MusicItemCard(
                musicItem = musicItem,
                viewModel = viewModel, // Pass the shared viewModel here
                isLoaded = index in loadedItems,
                onImageLoaded = {
                    loadedItems = loadedItems + index
                },
                onClick = {
                    coroutineScope.launch {
                        val musicFilePath = retrieveMusicFile(
                            context,
                            TELEGRAM_BOT_TOKEN,
                            musicItem.stream_id
                        )

                        if (musicFilePath != null) {
                            viewModel.setCurrentSong(context, musicItem, musicFilePath)
                            navController.navigate("player")
                        } else {
                            Log.e("MusicItemsGrid", "Failed to fetch music file for ${musicItem.title}")
                            // Optionally show a snackbar / toast
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MusicItemCard(
    musicItem: Song,
    viewModel: TabViewModel, // Remove the default parameter
    isLoaded: Boolean,
    onImageLoaded: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color(0xFF444444)) }

    // Pop animation when item loads
    val scale by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "popScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = tween(300),
        label = "popAlpha"
    )

    LaunchedEffect(musicItem.id) {
        val request = ImageRequest.Builder(context)
            .data(IMAGE_BUCKET_URL + musicItem.id + ".webp")
            .allowHardware(false)
            .build()
        val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
        result?.let { drawable ->
            Palette.from(drawable.toBitmap()).generate { palette ->
                palette?.getDominantColor(0xFF444444.toInt())?.let {
                    dominantColor = Color(it)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { if (isLoaded) onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Record rings
            Canvas(modifier = Modifier.size(180.dp)) {
                val radius = size.minDimension / 2
                for (i in 1..18) {
                    drawCircle(
                        color = Color(0xFF1A1A1A),
                        radius = radius * (i / 18f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(dominantColor.copy(alpha = 0.4f), Color.Transparent),
                        radius = radius * 0.9f
                    ),
                    radius = radius * 0.9f
                )
            }

            // Load image
            SubcomposeAsyncImage(
                model = IMAGE_BUCKET_URL + musicItem.id + ".webp",
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    onImageLoaded?.invoke()
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = musicItem.title,
            style = CustomTypography.bodyMedium,
            color = AppColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = musicItem.artist ?: "Unknown Artist",
            style = CustomTypography.bodySmall,
            color = AppColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}