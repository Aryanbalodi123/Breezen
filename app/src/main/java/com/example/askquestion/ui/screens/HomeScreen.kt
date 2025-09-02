package com.example.askquestion.ui.screens

import com.example.askquestion.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography


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
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }
}


@Composable
fun HomeContent(navController: NavController) {
    // Create sound items data once with unique IDs
    val soundItems = remember {
        listOf(
            SoundData(1, "Ocean Waves", "Natural water sounds", "32 min", AppColors.LightGreen),
            SoundData(2, "Rainfall", "Gentle rain drops", "45 min", AppColors.PrimaryGreen),
            SoundData(3, "Heavy Rain", "Deep rain sounds", "45 min", AppColors.PrimaryGreen),
            SoundData(4, "Light Rain", "Soft rain drops", "45 min", AppColors.PrimaryGreen),
            SoundData(5, "Thunder Storm", "Rain with thunder", "45 min", AppColors.PrimaryGreen),
            SoundData(6, "Forest Birds", "Morning bird songs", "28 min", AppColors.Yellow),
            SoundData(7, "Light Rain", "Soft rain drops", "45 min", AppColors.PrimaryGreen),
            SoundData(8, "Thunder Storm", "Rain with thunder", "45 min", AppColors.PrimaryGreen),
            SoundData(9, "Forest Birds", "Morning bird songs", "28 min", AppColors.Yellow),
            SoundData(10, "Forest Birds", "Morning bird songs", "28 min", AppColors.Yellow),
            SoundData(11, "Light Rain", "Soft rain drops", "45 min", AppColors.PrimaryGreen),
            SoundData(12, "Thunder Storm", "Rain with thunder", "45 min", AppColors.PrimaryGreen),
            SoundData(13, "Forest Birds", "Morning bird songs", "28 min", AppColors.Yellow)
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        val backgroundBrush = AppBackground()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
        )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 120.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        HeaderSection()
        Spacer(modifier = Modifier.height(28.dp))

        CategoriesSection()
        Spacer(modifier = Modifier.height(28.dp))

        FeaturedSection(navController)
        Spacer(modifier = Modifier.height(28.dp))

        PopularSoundsHeader(onSeeAllClick = { /* Handle see all */ })
        Spacer(modifier = Modifier.height(16.dp))

        soundItems.forEach { soundData ->
            OptimizedSoundItem(
                soundData = soundData,
                onClick = { null }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }}
}

@Stable
data class SoundData(
    val id: Int,
    val title: String,
    val subtitle: String,
    val duration: String,
    val color: Color
)

@Composable
fun HeaderSection() {
        val headerTextStyle = remember { CustomTypography.headlineLarge }
        val subTextStyle = remember { CustomTypography.bodyMedium }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning",
                style = subTextStyle,
                color = AppColors.TextSecondary
            )
            Text(
                text = "Aryan",
                style = headerTextStyle,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val buttonColors = remember {
            ButtonColors(
                background = AppColors.GlassBackground,
                border = AppColors.GlassBorder
            )
        }

        IconButton(
            onClick = { /* Handle notification */ },
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = buttonColors.background,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = buttonColors.border,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Stable
data class ButtonColors(
    val background: Color,
    val border: Color
)

@Composable
fun CategoriesSection() {
    val categories = remember {
        listOf(
            CategoryData("Sleep", AppColors.PrimaryGreen),
            CategoryData("Focus", AppColors.Yellow),
            CategoryData("Relax", AppColors.LightGreen),
            CategoryData("Meditate", AppColors.DarkGreen)
        )
    }

    Column {
        Text(
            text = "Categories",
            style = CustomTypography.titleLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(
                items = categories,
                key = { it.name }
            ) { categoryData ->
                CategoryChip(categoryData)
            }
        }
    }
}

@Stable
data class CategoryData(
    val name: String,
    val color: Color
)

@Composable
fun CategoryChip(categoryData: CategoryData) {
    val chipStyling = remember(categoryData.color) {
        ChipStyling(
            background = AppColors.CardBackground,
            border = categoryData.color.copy(alpha = 0.3f),
            textColor = categoryData.color,
            shape = RoundedCornerShape(25.dp)
        )
    }

    Box(
        modifier = Modifier
            .clip(chipStyling.shape)
            .background(chipStyling.background)
            .border(
                width = 1.dp,
                color = chipStyling.border,
                shape = chipStyling.shape
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication =ripple(
        bounded = true,   // ripple stays inside bounds
        radius = 24.dp
    )) { }
    ) {
        Text(
            text = categoryData.name,
            style = CustomTypography.bodyMedium,
            color = chipStyling.textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Stable
data class ChipStyling(
    val background: Color,
    val border: Color,
    val textColor: Color,
    val shape: RoundedCornerShape
)

@Composable
fun FeaturedSection(navController: NavController) {
    val featuredStyling = remember {
        FeaturedStyling(
            gradient = Brush.horizontalGradient(
                colors = listOf(
                    AppColors.PrimaryGreen.copy(alpha = 0.1f),
                    AppColors.LightGreen.copy(alpha = 0.05f)
                )
            ),
            border = AppColors.PrimaryGreen.copy(alpha = 0.2f),
            shape = RoundedCornerShape(24.dp)
        )
    }

//    }

        Column(
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
                .background(Color(0xFF012f46))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // 🔹 Background circle pinned to top-right
                Image(
                    painter = painterResource(R.drawable.gradient_circles),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)              // adjust size to your liking
                        .align(Alignment.TopEnd)   // sticks to top-right
                )

                // 🔹 Foreground content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "10:00 min",
                        color = Color.White,
                        modifier = Modifier.background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Quiet Flight",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "Take a journey through quiet sanctuary and blissful resonance",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }




}

@Stable
data class FeaturedStyling(
    val gradient: Brush,
    val border: Color,
    val shape: RoundedCornerShape
)

@Composable
fun PopularSoundsHeader(onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Popular Sounds",
            style = CustomTypography.titleLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "See all",
            style = CustomTypography.bodyMedium,
            color = AppColors.PrimaryGreen,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onSeeAllClick() }
        )
    }
}

@Composable
fun OptimizedSoundItem(
    soundData: SoundData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemStyling = remember(soundData.color) {
        SoundItemStyling(
            iconBackground = soundData.color.copy(alpha = 0.1f),
            iconBorder = soundData.color.copy(alpha = 0.3f),
            cardBorder = soundData.color.copy(alpha = 0.2f),
            iconTint = soundData.color,
            playTint = soundData.color,
            cardShape = RoundedCornerShape(16.dp),
            iconShape = RoundedCornerShape(12.dp)
        )
    }

    val titleStyle = remember { CustomTypography.bodyLarge }
    val subtitleStyle = remember { CustomTypography.bodySmall }
    val durationStyle = remember { CustomTypography.bodySmall }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(itemStyling.cardShape)
            .background(AppColors.CardBackground)
            .border(
                width = 1.dp,
                color = itemStyling.cardBorder,
                shape = itemStyling.cardShape
            )
            .clickable(    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(),) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(itemStyling.iconShape)
                .background(itemStyling.iconBackground)
                .border(
                    width = 1.dp,
                    color = itemStyling.iconBorder,
                    shape = itemStyling.iconShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Sound",
                tint = itemStyling.iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = soundData.title,
                style = titleStyle,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = soundData.subtitle,
                style = subtitleStyle,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = soundData.duration,
                style = durationStyle,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = itemStyling.playTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Stable
data class SoundItemStyling(
    val iconBackground: Color,
    val iconBorder: Color,
    val cardBorder: Color,
    val iconTint: Color,
    val playTint: Color,
    val cardShape: RoundedCornerShape,
    val iconShape: RoundedCornerShape
)
