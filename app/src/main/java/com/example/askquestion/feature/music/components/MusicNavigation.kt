package com.example.askquestion.feature.music.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.askquestion.core.network.Category
import com.example.askquestion.core.network.Tab
import com.example.askquestion.core.ui.theme.AppWhite
import com.example.askquestion.core.ui.theme.BrandGreen
import com.example.askquestion.core.ui.theme.BrandGreenBright
import com.example.askquestion.core.ui.theme.BrandGreenDarker
import com.example.askquestion.core.ui.theme.SystemError
import com.example.askquestion.core.ui.theme.SystemPause
import com.example.askquestion.core.ui.theme.SystemStop
import com.example.askquestion.core.ui.theme.SystemWarning
import com.example.askquestion.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

// ------------------------------------------------------------
//                      TOP TAB ROW (UNCHANGED)
// ------------------------------------------------------------
@Composable
internal fun TabButtonRow(
    tabs: List<Tab>, selectedIndex: Int, onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))

        tabs.forEachIndexed { index, tab ->
            TabItem(
                title = tab.name,
                isSelected = index == selectedIndex,
                onClick = { onTabSelected(index) }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun TabItem(
    title: String, isSelected: Boolean, onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) BrandGreenBright.copy(alpha = 0.20f) else Color.Transparent,
        animationSpec = tween(300)
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) BrandGreenBright else TextSecondary,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}



// ------------------------------------------------------------
//                        CATEGORY ROW
// ------------------------------------------------------------
@Composable
internal fun CategoryFilterChips(
    categories: List<Category>, selectedIndex: Int, onCategorySelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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



// ------------------------------------------------------------
//   ⭐ MINIMAL CHIPS WITH COLORED UNDERLINE
// ------------------------------------------------------------
@Composable
private fun EnhancedCategoryFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Color palette from your theme
    val chipColors = listOf(
        BrandGreen,
        BrandGreenBright,
        BrandGreenDarker,
        SystemError,
        SystemWarning,
        SystemStop,
        SystemPause
    )

    val accentColor = remember(text) {
        chipColors[(text.hashCode().absoluteValue) % chipColors.size]
    }

    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.12f else 0.05f,
        animationSpec = tween(300)
    )

    val underlineWidth by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) AppWhite else TextSecondary,
        animationSpec = tween(250)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = accentColor.copy(alpha = 0.2f)),
                    onClick = {
                        pressed = true
                        onClick()
                    }
                )
                .padding(horizontal = 20.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                letterSpacing = 0.3.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Animated colored underline
        Box(
            modifier = Modifier
                .width((text.length * 4).dp * underlineWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }
}