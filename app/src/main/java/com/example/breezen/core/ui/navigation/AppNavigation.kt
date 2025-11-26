package com.example.breezen.core.ui.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets // Added Import
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding // Added Import
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.breezen.core.data.OnboardingPreferences
import com.example.breezen.core.ui.components.EnhancedBottomNavigation
import com.example.breezen.core.ui.util.overshootEasing
import com.example.breezen.feature.breathe.BreatheScreen
import com.example.breezen.feature.chatbot.ChatBotScreen
import com.example.breezen.feature.chatbot.ChatViewModel
import com.example.breezen.feature.home.HomeContent
import com.example.breezen.feature.meditation.MeditationGuidedScreen
import com.example.breezen.feature.meditation.MeditationPlayer
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.music.MusicScreen
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.onboarding.OnboardingScreen
import com.example.breezen.feature.player.PlayerScreen
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay

@Stable
class AppNavigationState(
    val navController: NavHostController,
    private val currentRoute: String?
) {
    val showBottomBar: Boolean
        get() {
            return currentRoute in listOf(
                "home",
                "music",
                "breathe",
                "player",
                "guided_meditate",
                "guided_meditate_player",
                "chatbot"
            )
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

    val chatViewModel: ChatViewModel = viewModel()
    val musicViewModel: TabViewModel = viewModel()
    val meditationViewModel: MeditationViewModel = viewModel()

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

    if (isOnboardingInitialized) {
        val startDestination = if (isOnboardingCompleted) "home" else "onboard"

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            // --- FIX: Tell Scaffold NOT to handle system windows automatically ---
            // This prevents it from adding a background or reserving space at the bottom.
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                AnimatedVisibility(
                    visible = navigationState.showBottomBar && showBottomNav,
                    modifier = Modifier
                        .fillMaxWidth()
                        // --- FIX: Manually pad ONLY the bottom bar ---
                        // navigationBarsPadding pushes the bar UP above the gesture area.
                        .navigationBarsPadding()
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
            // We ignore innerPadding so content goes behind the bottom bar (which is what we want for transparency)
            NavHost(
                navController = navigationState.navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
                composable("onboard") {
                    OnboardingScreen(
                        navController = navigationState.navController,
                        onOnboardingComplete = handleOnboardingComplete
                    )
                }
                composable("home") {
                    HomeContent(
                        navController = navigationState.navController,
                        viewModel = musicViewModel
                    )
                }
                composable("music") {
                    MusicScreen(
                        navController = navigationState.navController,
                        viewModel = musicViewModel
                    )
                }
                composable("player") {
                    PlayerScreen(
                        navController = navigationState.navController,
                        viewModel = musicViewModel
                    )
                }
                composable("breathe") {
                    BreatheScreen(navigationState.navController)
                }
                composable("guided_meditate") {
                    MeditationGuidedScreen(
                        navController = navigationState.navController,
                        viewModel = meditationViewModel
                    )
                }
                composable("guided_meditate_player") {
                    MeditationPlayer(
                        navController = navigationState.navController,
                        viewModel = meditationViewModel
                    )
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