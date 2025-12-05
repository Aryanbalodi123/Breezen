@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.breezen.core.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breezen.R
import com.example.breezen.core.ui.navigation.AppNavigationState
import com.example.breezen.feature.chatbot.ChatViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavigationBar(
    navigation: AppNavigationState,
    hazeState: HazeState,
    chatViewModel: ChatViewModel
) {
    val isChatScreen = navigation.isCurrentRoute("chatbot")

    val barHeight = 64.dp
    val barCornerRadius = 50.dp
    val closeButtonSize = 48.dp

    val targetBg = if (isChatScreen) Color.Black.copy(alpha = 0.10f)
    else Color.White.copy(alpha = 0.12f)

    val animatedBg by androidx.compose.animation.animateColorAsState(
        targetBg, tween(200), label = "nav_bg"
    )

    val glassHazeStyle = remember {
        HazeStyle(
            blurRadius = 14.dp,
            tint = HazeTint(Color.Black.copy(alpha = 0.35f)),
            noiseFactor = 0f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(barCornerRadius))
                    .hazeEffect(state = hazeState, style = glassHazeStyle)
                    .background(animatedBg),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = isChatScreen,
                    animationSpec = tween(180),
                    label = "NavCrossfade"
                ) { isChat ->
                    if (isChat) ChatInputBar(chatViewModel)
                    else FluidBottomBar(navigation)
                }
            }

            AnimatedVisibility(
                visible = isChatScreen,
                enter = fadeIn(tween(140)),
                exit = fadeOut(tween(140))
            ) {
                Box(
                    modifier = Modifier
                        .size(closeButtonSize)
                        .clip(CircleShape)
                        .hazeEffect(state = hazeState, style = glassHazeStyle)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { navigation.navigateTo("home") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Chat",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatInputBar(chatViewModel: ChatViewModel) {

    var input by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank()) {
                        chatViewModel.sendMessage(input.trim())
                        input = ""
                        keyboardController?.hide()
                    }
                }),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (input.isEmpty()) {
                            Text(
                                "Message Zeni...",
                                color = Color.White.copy(alpha = 0.48f),
                                fontSize = 15.sp
                            )
                        }
                        inner()
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = input.isNotBlank(),
            enter = scaleIn(spring(dampingRatio = 0.6f)) + fadeIn(),
            exit = scaleOut(spring(dampingRatio = 0.6f)) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        if (input.isNotBlank()) {
                            chatViewModel.sendMessage(input.trim())
                            input = ""
                            keyboardController?.hide()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

data class FluidNavItem(
    val route: String,
    val iconRes: Int,
    val description: String
)

@Composable
fun FluidBottomBar(navigation: AppNavigationState) {

    val items = remember {
        listOf(
            FluidNavItem("home", R.drawable.home, "Home"),
            FluidNavItem("music", R.drawable.music, "Music"),
            FluidNavItem("chatbot", R.drawable.chatbot, "Chat"),
            FluidNavItem("breathe", R.drawable.breathe, "Breathe"),
            FluidNavItem("guided_meditate", R.drawable.guided_meditation, "Sleep")
        )
    }

    val selectedIndex by remember(navigation.currentRoute) {
        derivedStateOf {
            items.indexOfFirst { it.route == navigation.currentRoute }
                .takeIf { it != -1 } ?: 0
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val itemWidth = totalWidth / items.size

        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
            label = "indicator_slide"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                FluidNavItemView(
                    item = item,
                    isSelected = index == selectedIndex,
                    width = itemWidth
                ) { navigation.navigateTo(item.route) }
            }
        }
    }
}

@Composable
fun FluidNavItemView(
    item: FluidNavItem,
    isSelected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by androidx.compose.animation.animateColorAsState(
        targetValue = Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "icon_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "icon_scale"
    )

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxSize()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.description,
            tint = iconColor,
            modifier = Modifier
                .size(26.dp)
                .scale(scale)
        )
    }
}
