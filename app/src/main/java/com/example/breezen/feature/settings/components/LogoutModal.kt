package com.example.breezen.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.SystemStop
import com.example.breezen.core.ui.theme.TextSecondary

@Composable
fun LogoutModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismissRequest) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.88f),
            exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(AppBlack.copy(alpha = 0.95f))
                    .border(
                        1.dp,
                        AppWhite.copy(alpha = 0.12f),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // HEADER ICON
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = SystemStop,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Log Out?",
                    style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppWhite
                )

                Text(
                    "You will be signed out of your account.",
                    style = AppTypography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SmallButton(
                        text = "Cancel",
                        bg = Color.Transparent,
                        textColor = AppWhite,
                        border = AppWhite.copy(alpha = 0.2f)
                    ) { onDismissRequest() }

                    SmallButton(
                        text = "Log Out",
                        bg = SystemStop,
                        textColor = AppWhite
                    ) { onConfirm() }
                }
            }
        }
    }
}

