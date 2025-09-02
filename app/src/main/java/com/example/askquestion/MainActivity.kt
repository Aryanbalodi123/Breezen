package com.example.askquestion

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.askquestion.theme.ASKQUESTIONTheme
import com.example.askquestion.theme.AppColors
import com.example.askquestion.ui.screens.PlayerScreen
import com.example.askquestion.ui.screens.SplashScreen
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel


import dev.chrisbanes.haze.*


import com.example.askquestion.ui.screens.BreatheScreen
import com.example.askquestion.ui.screens.HomeContent
import com.example.askquestion.ui.screens.MeditateScreen
import com.example.askquestion.ui.screens.MusicScreen
import com.example.askquestion.ui.screens.TabViewModel
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private lateinit var onboardingPreferences: OnboardingPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        onboardingPreferences = OnboardingPreferences(this)

        setContent {
            ASKQUESTIONTheme {
                // Just apply system bar insets here
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets
                            .systemBars
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
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
            val shouldShow = currentRoute in listOf("home", "music", "breathe", "player" , "meditate")
            return shouldShow
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
// REMEMBER FUNCTION
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



@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AppNavHost(onboardingPreferences: OnboardingPreferences) {
    val navigationState = rememberAppNavigationState()

    // Create a single HazeState for the entire app
    val hazeState = rememberHazeState()

    var isOnboardingCompleted by remember { mutableStateOf(false) }
    var isOnboardingInitialized by remember { mutableStateOf(false) }

    var showBottomNav by remember { mutableStateOf(false) }

    // Initialize onboarding state immediately
    LaunchedEffect(Unit) {
        val completed = onboardingPreferences.isOnboardingCompleted()
        isOnboardingCompleted = completed
        isOnboardingInitialized = true
    }

    // Handle onboarding completion
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
if (isOnboardingInitialized){
    val startDestination = if (isOnboardingCompleted) "home" else "onboard"

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content area - this will be the source for blurring
        NavHost(
            navController = navigationState.navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState) // Mark background content for blurring
        ) {
            composable("onboard") {
                SplashScreen(
                    navController = navigationState.navController,
                    onOnboardingComplete = { handleOnboardingComplete() }
                )
            }
            composable("home") {
                HomeContent(navigationState.navController)
            }
            composable("breathe") {
                BreatheScreen(navigationState.navController)
            }
//            composable("sounds") {
//                SoundsScreen(navigationState.navController)
//            }
            composable("music") {
                val parentEntry = remember(this) {
                    navigationState.navController.getBackStackEntry("music")
                }
                val sharedViewModel: TabViewModel = viewModel(parentEntry)
                MusicScreen(
                    viewModel = sharedViewModel,
                    navController = navigationState.navController
                )
            }

            composable("player") {
                val parentEntry = remember(this) {
                    navigationState.navController.getBackStackEntry("music")
                }
                val sharedViewModel: TabViewModel = viewModel(parentEntry)
                PlayerScreen(
                    navController = navigationState.navController,
                    viewModel = sharedViewModel
                )
            }
            composable("meditate") {
                MeditateScreen(navController = navigationState.navController)
            }
        }

        // Bottom navigation with haze effect
        AnimatedVisibility(
            visible = navigationState.showBottomBar && showBottomNav,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = overshootEasing(1.2f)
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            BottomNavigation(
                navigation = navigationState,
                hazeState = hazeState // Pass the shared hazeState
            )
        }
    }}
}

@Composable
fun BottomNavigation(
    navigation: AppNavigationState,
    hazeState: HazeState
) {
    val reflectionGradient = remember {
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = .15f), Color.Transparent),
            start = Offset.Zero,
            end = Offset(0f, 20f)
        )
    }

    val shape = RoundedCornerShape(35.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 32.dp)
            .clip(shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = 25.dp,
                        tint = HazeTint(Color.White.copy(alpha = 0.2f)),
                        noiseFactor = 0f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(reflectionGradient, shape)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
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
                isSelected = navigation.isCurrentRoute("meditate"),
                onClick = { navigation.navigateTo("meditate") }
            )
        }
    }
}

// Helper functions
fun OvershootInterpolator.toEasing(): Easing {
    return Easing { fraction ->
        this.getInterpolation(fraction)
    }
}

fun overshootEasing(tension: Float = 2f): Easing {
    return Easing { fraction ->
        val t = fraction - 1.0f
        t * t * ((tension + 1.0f) * t + tension) + 1.0f
    }
}

// NAV ITEM with animations
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




