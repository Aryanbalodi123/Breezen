package com.example.breezen.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.BrandGreenBright
import com.example.breezen.core.ui.theme.GlassBackground

@Composable
fun SuccessToast(
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Box(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GlassBackground,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, BrandGreen, RoundedCornerShape(18.dp))
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    Text(
                        message,
                        color = BrandGreenBright,
                        style = AppTypography.bodyMedium
                    )
                }
            }
        }
    }
}
