package com.example.askquestion.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.askquestion.core.ui.navigation.AppNavigationState
import com.example.askquestion.feature.chatbot.ChatViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun EnhancedBottomNavigation(
    navigation: AppNavigationState,
    hazeState: HazeState,
    chatViewModel: ChatViewModel
) {
    // Glass effects are special and not part of the theme, so they can stay
    val reflectionGradient = remember {
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = .15f), Color.Transparent),
            start = Offset.Zero,
            end = Offset(0f, 20f)
        )
    }

    val shape = RoundedCornerShape(35.dp)
    val isChatScreen = navigation.isCurrentRoute("chatbot")

    var showChatMode by remember(isChatScreen) { mutableStateOf(isChatScreen) }

    val rowWidth = if (isChatScreen) 1f else 0.8f

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(rowWidth),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp)
                    .clip(shape)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 25.dp,
                            tint = HazeTint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            noiseFactor = 0f
                        )
                    )
                    .background(reflectionGradient, shape)
            ) {
                AnimatedContent(
                    targetState = showChatMode,
                    transitionSpec = {
                        if (targetState) {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        } else {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        }
                    },
                    label = "bottom_nav_content"
                ) { isChatModeActive ->
                    if (isChatModeActive) {
                        ChatInputBar(chatViewModel = chatViewModel)
                    } else {
                        NormalBottomBar(navigation = navigation)
                    }
                }
            }

            if (isChatScreen) {
                FloatingToggleButton(
                    isInChatMode = showChatMode,
                    onClick = { showChatMode = !showChatMode }
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    chatViewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    RoundedCornerShape(24.dp)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
            decorationBox = { innerTextField ->
                if (input.isEmpty()) {
                    Text(
                        "Ask me anything...",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = LocalTextStyle.current
                    )
                }
                innerTextField()
            }
        )

        AnimatedVisibility(
            visible = input.isNotBlank(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            IconButton(
                onClick = {
                    chatViewModel.sendMessage(input.trim())
                    input = ""
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingToggleButton(
    isInChatMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "toggle_scale"
    )

    // Using systemError from our theme for the red
    val backgroundColor by animateColorAsState(
        targetValue = if (isInChatMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(400),
        label = "toggle_bg_color"
    )
    val iconColor = if (isInChatMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .size(70.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = .15f), Color.Transparent),
                        start = Offset.Zero,
                        end = Offset(0f, 20f)
                    ),
                    CircleShape
                )
                .background(backgroundColor.copy(alpha = 0.9f), CircleShape)
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isInChatMode,
                label = "toggle_icon_anim",
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(
                        tween(300),
                        initialScale = 0.7f
                    )) togetherWith
                            (fadeOut(tween(200)) + scaleOut(
                                tween(200),
                                targetScale = 0.7f
                            ))
                }
            ) { isChat ->
                if (isChat) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = "Switch to navigation",
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        Icons.Outlined.QuestionAnswer,
                        contentDescription = "Switch to chat",
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NormalBottomBar(
    navigation: AppNavigationState
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            isSelected = navigation.isCurrentRoute("home"),
            onClick = { navigation.navigateTo("home") }
        )
        NavItem(
            icon = Icons.Outlined.MusicNote,
            selectedIcon = Icons.Filled.MusicNote,
            isSelected = navigation.isCurrentRoute("music"),
            onClick = { navigation.navigateTo("music") }
        )
        NavItem(
            icon = Icons.Outlined.EnergySavingsLeaf,
            selectedIcon = Icons.Filled.GraphicEq, // Note: This was GraphicEq in original
            isSelected = navigation.isCurrentRoute("breathe"),
            onClick = { navigation.navigateTo("breathe") }
        )
        NavItem(
            icon = Icons.Outlined.Bed,
            selectedIcon = Icons.Filled.GraphicEq, // Note: This was GraphicEq in original
            isSelected = navigation.isCurrentRoute("chatbot"),
            onClick = { navigation.navigateTo("chatbot") }
        )
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        },
        animationSpec = tween(200),
        label = "icon_color"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    val currentIcon = if (isSelected) selectedIcon else icon

    Icon(
        imageVector = currentIcon,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp)
            .scale(iconScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        tint = iconTint
    )
}