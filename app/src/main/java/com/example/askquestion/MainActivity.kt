package com.example.askquestion

import MeditateScreen
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.askquestion.theme.ASKQUESTIONTheme
import com.example.askquestion.theme.AppColors
import com.example.askquestion.ui.screens.BreatheScreen
import com.example.askquestion.ui.screens.ChatBotScreen
import com.example.askquestion.ui.screens.ChatViewModel
import com.example.askquestion.ui.screens.HomeContent
import com.example.askquestion.ui.screens.MusicScreen
import com.example.askquestion.ui.screens.OnboardingScreen
import com.example.askquestion.ui.screens.PlayerScreen
import com.example.askquestion.ui.screens.TabViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var onboardingPreferences: OnboardingPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.navigationBarColor = android.graphics.Color.BLACK

        onboardingPreferences = OnboardingPreferences(this)

        setContent {
            ASKQUESTIONTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    AppNavHost(onboardingPreferences)
                }
            }
        }
    }
}

class OnboardingPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }
}

@Stable
class AppNavigationState(
    val navController: NavHostController,
    private val currentRoute: String?
) {
    val showBottomBar: Boolean
        get() {
            return currentRoute in listOf("home", "music", "breathe", "player", "meditate", "chatbot")
        }

    val currentRouteName: String?
        get() = currentRoute

    fun isCurrentRoute(route: String): Boolean {
        return currentRouteName == route
    }

    fun navigateTo(route: String) {
        if (currentRouteName != route) {
            navController.navigate(route) {
                launchSingleTop = true
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                restoreState = true
            }
        }
    }
}

@Composable
fun rememberAppNavigationState(
    navController: NavHostController = rememberNavController()
): AppNavigationState {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    return remember(navController, currentRoute) {
        AppNavigationState(navController, currentRoute)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedGetBackStackEntry")
@Composable
fun AppNavHost(onboardingPreferences: OnboardingPreferences) {
    val navigationState = rememberAppNavigationState()
    val hazeState = rememberHazeState()

    var isOnboardingCompleted by remember { mutableStateOf(false) }
    var isOnboardingInitialized by remember { mutableStateOf(false) }
    var showBottomNav by remember { mutableStateOf(false) }

    // Shared ViewModels
    val chatViewModel: ChatViewModel = viewModel()
    val tabViewModel: TabViewModel = viewModel() // 争 Shared across screens

    // --- Onboarding setup ---
    LaunchedEffect(Unit) {
        val completed = onboardingPreferences.isOnboardingCompleted()
        isOnboardingCompleted = completed
        isOnboardingInitialized = true
        Log.d("AppNavHost", "Onboarding check complete. Is completed: $completed")
    }

    val handleOnboardingComplete = {
        onboardingPreferences.setOnboardingCompleted(true)
        isOnboardingCompleted = true
        navigationState.navController.navigate("home") {
            popUpTo("onboard") { inclusive = true }
        }
    }

    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted) {
            delay(1500)
            showBottomNav = true
        } else {
            showBottomNav = false
        }
    }

    // --- NavHost ---
    if (isOnboardingInitialized) {
        val startDestination = if (isOnboardingCompleted) "home" else "onboard"

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = navigationState.showBottomBar && showBottomNav,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(400, easing = overshootEasing(1.2f))
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    ) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))
                ) {
                    EnhancedBottomNavigation(
                        navigation = navigationState,
                        hazeState = hazeState,
                        chatViewModel = chatViewModel
                    )
                }
            }
        ) { _ ->
            NavHost(
                navController = navigationState.navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
                composable("onboard") {
                    // Pass the completion handler to the onboarding screen
                    OnboardingScreen(
                        navController = navigationState.navController,
                        onOnboardingComplete = handleOnboardingComplete
                    )
                }

                // --- REMOVED composable("profile_setup") ---
                // The profile setup route is no longer needed.

                // --- Home ---
                composable("home") {
                    HomeContent(
                        navController = navigationState.navController,
                        viewModel = tabViewModel // 争 Shared ViewModel
                    )
                }

                // --- Music ---
                composable("music") {
                    MusicScreen(
                        navController = navigationState.navController,
                        viewModel = tabViewModel // 争 SAME INSTANCE
                    )
                }

                // --- Player ---
                composable("player") {
                    PlayerScreen(
                        navController = navigationState.navController,
                        viewModel = tabViewModel // 争 SAME INSTANCE
                    )
                }

                // --- Other screens ---
                composable("breathe") {
                    BreatheScreen(navigationState.navController)
                }
                composable("meditate") {
                    MeditateScreen(navigationState.navController)
                }

                composable("chatbot") {
                    ChatBotScreen(
                        navController = navigationState.navController,
                        viewModel = chatViewModel
                    )
                }
            }
        }
    }
}


@Composable
fun EnhancedBottomNavigation(
    navigation: AppNavigationState,
    hazeState: HazeState,
    chatViewModel: ChatViewModel
) {
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
    var rowWidth = if(isChatScreen) 1f else 0.8f
    Box(
        modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 20.dp),
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
                            tint = HazeTint(Color.White.copy(alpha = 0.2f)),
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
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(24.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            decorationBox = { innerTextField ->
                if (input.isEmpty()) {
                    Text(
                        "Ask me anything...",
                        color = Color.White.copy(alpha = 0.6f),
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
                    .background(Color(0xFF69F0AE), CircleShape)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
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

    val backgroundColor by animateColorAsState(
        targetValue = if (isInChatMode) Color(0xFFE91E63) else Color(0xFF69F0AE),
        animationSpec = tween(400),
        label = "toggle_bg_color"
    )
    val iconColor = if (isInChatMode) Color.White else Color.Black

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
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
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
            selectedIcon = Icons.Filled.GraphicEq,
            isSelected = navigation.isCurrentRoute("breathe"),
            onClick = { navigation.navigateTo("breathe") }
        )
        NavItem(
            icon = Icons.Outlined.Bed,
            selectedIcon = Icons.Filled.GraphicEq,
            isSelected = navigation.isCurrentRoute("chatbot"),
            onClick = { navigation.navigateTo("chatbot") }
        )
    }
}

fun overshootEasing(tension: Float = 2f): Easing {
    return Easing { fraction ->
        val t = fraction - 1.0f
        t * t * ((tension + 1.0f) * t + tension) + 1.0f
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
            AppColors.TextPrimary
        } else {
            AppColors.TextSecondary.copy(alpha = 0.9f)
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