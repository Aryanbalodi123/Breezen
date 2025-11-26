package com.example.breezen.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.breezen.R
import com.example.breezen.core.ui.navigation.AppNavigationState
import com.example.breezen.feature.chatbot.ChatViewModel
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
    val isChatScreen = navigation.isCurrentRoute("chatbot")

    // Common Glass Styling
    val reflectionGradient = remember {
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
            start = Offset.Zero,
            end = Offset(0f, 40f)
        )
    }

    val glassHazeStyle = HazeStyle(
        blurRadius = 30.dp,
        tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
        noiseFactor = 0f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --- MAIN BAR (Navigation OR Input) ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .hazeEffect(state = hazeState, style = glassHazeStyle)
                    .background(reflectionGradient)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isChatScreen,
                    transitionSpec = {
                        if (targetState) {
                            (slideInHorizontally { height -> height } + fadeIn()).togetherWith(
                                slideOutHorizontally { height -> -height } + fadeOut())
                        } else {
                            (slideInHorizontally { height -> -height } + fadeIn()).togetherWith(
                                slideOutHorizontally { height -> height } + fadeOut())
                        }
                    },
                    label = "NavContent"
                ) { isChat ->
                    if (isChat) {
                        ChatInputBar(chatViewModel = chatViewModel)
                    } else {
                        NormalBottomBar(navigation = navigation)
                    }
                }
            }

            // --- TOGGLE BUTTON ---
            AnimatedVisibility(
                visible = isChatScreen,
                enter = scaleIn(spring(dampingRatio = 0.6f)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .hazeEffect(state = hazeState, style = glassHazeStyle)
                        .background(reflectionGradient)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        .clickable { navigation.navigateTo("home") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Chat",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // --- CENTER FAB (Only in Home) ---
        androidx.compose.animation.AnimatedVisibility(
            visible = !isChatScreen,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CenterChatButton(onClick = { navigation.navigateTo("chatbot") })
        }
    }
}

@Composable
fun ChatInputBar(
    chatViewModel: ChatViewModel
) {
    var input by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (input.isNotBlank()) {
                    chatViewModel.sendMessage(input.trim())
                    input = ""
                    keyboardController?.hide()
                }
            }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (input.isEmpty()) {
                        Text(
                            "Ask AI...",
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        AnimatedVisibility(visible = input.isNotBlank()) {
            IconButton(
                onClick = {
                    chatViewModel.sendMessage(input.trim())
                    input = ""
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NormalBottomBar(
    navigation: AppNavigationState
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side
        NavItem(
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            isSelected = navigation.isCurrentRoute("home"),
            onClick = { navigation.navigateTo("home") }
        )

        // ⭐ Reduced spacing between icons (was 16.dp)
        Spacer(modifier = Modifier.width(4.dp))

        NavItem(
            icon = Icons.Outlined.MusicNote,
            selectedIcon = Icons.Filled.MusicNote,
            isSelected = navigation.isCurrentRoute("music"),
            onClick = { navigation.navigateTo("music") }
        )

        // Center Space for FAB
        Spacer(modifier = Modifier.width(80.dp))

        // Right Side
        NavItem(
            icon = Icons.Outlined.EnergySavingsLeaf,
            selectedIcon = Icons.Filled.GraphicEq,
            isSelected = navigation.isCurrentRoute("breathe"),
            onClick = { navigation.navigateTo("breathe") }
        )

        // ⭐ Reduced spacing between icons (was 16.dp)
        Spacer(modifier = Modifier.width(4.dp))

        NavItem(
            icon = Icons.Outlined.Bed,
            selectedIcon = Icons.Default.AcUnit,
            isSelected = navigation.isCurrentRoute("guided_meditate"),
            onClick = { navigation.navigateTo("guided_meditate") }
        )
    }
}

@Composable
fun CenterChatButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "fab_float")

    // Image bobbing animation
    val panX by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_x"
    )

    val panY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_y"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "fab_scale"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(buttonScale)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                        )
                    )
                )
        )

        // Image moves inside static circle
        Image(
            painter = painterResource(id = R.drawable.navbar_chatbot),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.3f)
                .offset(x = panX.dp, y = panY.dp)
        )

        // Optional Overlay Icon
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        Icon(
            imageVector = Icons.Outlined.QuestionAnswer,
            contentDescription = "AI Chat",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.5f
        ),
        label = "icon_tint"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "icon_scale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(26.dp)
                    .scale(iconScale)
            )

            // Spacing between icon and dot
            Spacer(modifier = Modifier.height(6.dp))

            // Indicator Dot
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}