package com.example.breezen.feature.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.core.data.OnboardingPreferences
import com.example.breezen.core.data.UserPreferences
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.SystemStop
import com.example.breezen.core.ui.theme.TextSecondary
import com.example.breezen.feature.settings.components.LogoutModal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Main Settings Screen.
 * Displays profile summary, app settings, and logout functionality.
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // --- Dependencies & State ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe username from DataStore
    val username by UserPreferences
        .getUsername(context)
        .collectAsState(initial = "You")

    // UI States for Modals
    var showPasswordModal by remember { mutableStateOf(false) }
    var showLogoutModal by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // --- Background Design ---
    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFF0F1110), Color(0xFF000000))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // --- Scrollable Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // 1. User Profile Section
            ProfileCard(username)

            Spacer(Modifier.height(40.dp))

            // 2. Security Section
            SectionHeader("Account & Security")
            Spacer(Modifier.height(16.dp))
            SettingCard(
                icon = Icons.Default.Lock,
                title = "Change Password",
                subtitle = "Update your login credentials"
            ) { showPasswordModal = true }

            Spacer(Modifier.height(32.dp))

            // 3. Info Section
            SectionHeader("Information")
            Spacer(Modifier.height(16.dp))
            SettingCard(
                icon = Icons.Default.Info,
                title = "Credits",
                subtitle = "Assets and attribution"
            ) { navController.navigate("settings_credits") }

            Spacer(Modifier.height(12.dp))

            SettingCard(
                icon = Icons.Default.Info,
                title = "About Developer",
                subtitle = "Behind the scenes"
            ) { navController.navigate("developer_page") }

            Spacer(Modifier.height(50.dp))

            // 4. Footer / Logout
            LogoutButton { showLogoutModal = true }

            // Bottom padding for scrolling
            Spacer(Modifier.height(100.dp))
        }

        // --- Overlays (Modals & Toasts) ---

        ChangePasswordModal(
            visible = showPasswordModal,
            onDismissRequest = { showPasswordModal = false },
            viewModel = viewModel
        )

        LogoutModal(
            visible = showLogoutModal,
            onDismissRequest = { showLogoutModal = false },
            onConfirm = {
                showLogoutModal = false
                // Logic: Clear data -> Reset flags -> Navigate to Onboarding
                scope.launch {
                    UserPreferences.clearAll(context)
                    OnboardingPreferences(context).setOnboardingCompleted(false)
                    viewModel.logout {
                        navController.navigate("onboard") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        )

        // Temporary toast notification logic
        successMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(1500)
                successMessage = null
            }
            SuccessToast(
                message = msg,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
            )
        }
    }
}

/**
 * Displays the user's avatar with a breathing glow animation and a time-aware greeting.
 */
@Composable
fun ProfileCard(username: String?) {
    val name = username ?: "User"

    // Calculate greeting based on system time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning,"
            in 12..17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = BrandGreen.copy(0.15f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1C221C), Color(0xFF111411))
                )
            )
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // --- Avatar with Glow Animation ---
            Box(contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition()
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                // Outer Glow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(BrandGreen.copy(alpha = glowAlpha), Color.Transparent)
                            )
                        )
                )

                // Actual Avatar Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandGreen)
                        .border(2.dp, Color(0xFF2A332A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().uppercase(),
                        style = AppTypography.headlineMedium.copy(
                            color = AppWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            // --- Text Content ---
            Column {
                Text(
                    text = greeting,
                    style = AppTypography.bodyMedium.copy(color = TextSecondary),
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = name,
                    style = AppTypography.headlineSmall.copy(color = AppWhite),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Standardized header for settings categories.
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = AppTypography.labelSmall.copy(
            color = BrandGreen,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        ),
        modifier = Modifier.padding(start = 8.dp)
    )
}

/**
 * A generic row for a settings item.
 * Includes a press-down animation.
 */
@Composable
fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    // Animation state for touch feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1D1A)) // Dark surface color
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset animation trigger
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }

        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrandGreen.copy(alpha = 0.15f)), // Subtle green tint
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // Text Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.bodyLarge.copy(color = AppWhite, fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppTypography.bodySmall.copy(color = TextSecondary.copy(alpha = 0.7f))
            )
        }

        // Navigation Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.4f)
        )
    }
}

/**
 * Specifically styled button for the Logout action (Danger zone).
 */
@Composable
fun LogoutButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF241818)) // Reddish dark background
            .border(1.dp, SystemStop.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = SystemStop
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Log Out",
                style = AppTypography.bodyLarge.copy(
                    color = SystemStop,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}