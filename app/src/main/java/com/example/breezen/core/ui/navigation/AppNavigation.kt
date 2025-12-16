package com.example.breezen.core.ui.navigation

// ------- IMPORTS -------
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.breezen.core.data.OnboardingPreferences
import com.example.breezen.core.ui.components.BottomNavigationBar
import com.example.breezen.core.ui.util.overshootEasing
import com.example.breezen.feature.breathe.BreatheScreen
import com.example.breezen.feature.chatbot.ChatBotScreen
import com.example.breezen.feature.chatbot.ChatViewModel
import com.example.breezen.feature.home.HomeContent
import com.example.breezen.feature.home.HomeViewModel
import com.example.breezen.feature.meditation.MeditationGuidedScreen
import com.example.breezen.feature.meditation.MeditationPlayer
import com.example.breezen.feature.meditation.MeditationViewModel
import com.example.breezen.feature.music.MusicScreen
import com.example.breezen.feature.music.TabViewModel
import com.example.breezen.feature.onboarding.OnboardingScreen
import com.example.breezen.feature.player.PlayerScreen
import com.example.breezen.feature.settings.CreditsScreen
import com.example.breezen.feature.settings.DeveloperProfileScreen
import com.example.breezen.feature.settings.SettingsScreen
import com.example.breezen.feature.settings.SettingsViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay

// ------- NAVIGATION STATE -------
// ------- Holds current route + helper utils -------
@Stable
class AppNavigationState(
    val navController: NavHostController,
    val currentRoute: String?
) {
    val currentRouteName: String?
        get() = currentRoute

    fun isCurrentRoute(route: String) = currentRouteName == route

    // Show bottom nav only on main pages
    val showBottomBar: Boolean
        get() = currentRoute in listOf(
            "home", "music", "breathe", "player",
            "guided_meditate", "guided_meditate_player",
            "chatbot", "settings"
        )

    // Safe navigation
    fun navigateTo(route: String) {
        if (currentRouteName != route) {
            navController.navigate(route) {
                launchSingleTop = true
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                restoreState = true
            }
        }
    }
}

// ------- REMEMBER NAV STATE -------
// ------- Observes route changes -------
@Composable
fun rememberAppNavigationState(
    navController: NavHostController = rememberNavController()
): AppNavigationState {
    val backstack by navController.currentBackStackEntryAsState()
    val route = backstack?.destination?.route

    return remember(navController, route) {
        AppNavigationState(navController, route)
    }
}

// ------- APP NAV HOST -------
// ------- Entire navigation graph + onboarding logic -------
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavHost(onboardingPreferences: OnboardingPreferences) {

    val navigationState = rememberAppNavigationState()
    val hazeState = rememberHazeState()

    // ------- ViewModels -------
    val chatViewModel: ChatViewModel = viewModel()
    val musicViewModel: TabViewModel = viewModel()
    val meditationViewModel: MeditationViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    // ------- Onboarding State -------
    var isOnboardingInitialized by remember { mutableStateOf(false) }
    var isOnboardingCompleted by remember { mutableStateOf(false) }
    var showBottomNav by remember { mutableStateOf(false) }

    // Load onboarding preference
    LaunchedEffect(Unit) {
        val completed = onboardingPreferences.isOnboardingCompleted()
        isOnboardingCompleted = completed
        isOnboardingInitialized = true
        Log.d("AppNavHost", "Onboarding completed: $completed")
    }

    // When onboarding finishes → go home
    val handleOnboardingComplete = {
        onboardingPreferences.setOnboardingCompleted(true)
        isOnboardingCompleted = true
        navigationState.navController.navigate("home") {
            popUpTo("onboard") { inclusive = true }
        }
    }

    // Delay bottom nav reveal (smooth intro)
    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted) {
            delay(1500)
            showBottomNav = true
        } else showBottomNav = false
    }

    // Prevent drawing if not initialized
    if (!isOnboardingInitialized) return

    val startDestination = if (isOnboardingCompleted) "home" else "onboard"

    // ------- MAIN UI SCAFFOLD -------
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),

        // ------- BOTTOM NAV BAR -------
        bottomBar = {
            AnimatedVisibility(
                visible = navigationState.showBottomBar && showBottomNav,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(400, easing = overshootEasing(1.2f))
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(280)
                ) + fadeOut()
            ) {
                BottomNavigationBar(
                    navigation = navigationState,
                    hazeState = hazeState,
                    chatViewModel = chatViewModel
                )
            }
        }
    ) { _ ->

        // ------- NAV HOST GRAPH -------
        NavHost(
            navController = navigationState.navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {

            // ------- ONBOARDING -------
            composable("onboard") {
                OnboardingScreen(
                    navController = navigationState.navController,
                    onOnboardingComplete = handleOnboardingComplete
                )
            }

            // ------- HOME -------
            composable("home") {
                HomeContent(
                    navController = navigationState.navController,
                    viewModel = musicViewModel,
                    meditationViewModel = meditationViewModel,
                    chatViewModel = chatViewModel,
                    homeViewModel = homeViewModel
                )
            }

            // ------- MUSIC -------
            composable("music") {
                MusicScreen(
                    navController = navigationState.navController,
                    viewModel = musicViewModel
                )
            }

            // ------- PLAYER -------
            composable("player") {
                PlayerScreen(
                    navController = navigationState.navController,
                    viewModel = musicViewModel
                )
            }

            // ------- BREATHING -------
            composable("breathe") {
                BreatheScreen(navigationState.navController)
            }

            // ------- MEDITATION -------
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

            // ------- DEVELOPER PAGE -------
            composable("developer_page") {
                DeveloperProfileScreen(
                    navigationState.navController,
                    settingsViewModel
                )
            }

            // ------- CHATBOT -------
            composable("chatbot") {
                ChatBotScreen(
                    navController = navigationState.navController,
                    viewModel = chatViewModel
                )
            }

            // ------- SETTINGS -------
            composable("settings") {
                SettingsScreen(navController = navigationState.navController)
            }

            composable("settings_credits") {
                CreditsScreen(navController = navigationState.navController)
            }

            // ------- TEMP LOGIN -------
            composable("login_screen") {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Login Screen Placeholder", color = Color.White)
                }
            }
        }
    }
}
