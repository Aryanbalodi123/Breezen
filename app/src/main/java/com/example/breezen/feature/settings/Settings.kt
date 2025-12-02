package com.example.breezen.feature.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.breezen.core.data.UserPreferences
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.BrandGreenBright
import com.example.breezen.core.ui.theme.SystemStop
import com.example.breezen.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vmState by viewModel.state.collectAsState()

    val username by UserPreferences
        .getUsername(context)
        .collectAsState(initial = "You")

    var showPasswordModal by remember { mutableStateOf(false) }
    var showLogoutModal by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Premium dark background with subtle green tint
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0A0E0A),
            Color(0xFF000000),
            Color(0xFF000000)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 100.dp
                )        ) {
            Spacer(Modifier.height(16.dp))

            // ============================================================
            //  PROFILE CARD - Premium Apple Style
            // ============================================================
            PremiumProfileCard(username)

            Spacer(Modifier.height(36.dp))

            // ============================================================
            //  SECURITY SECTION
            // ============================================================
            SectionHeader("Security")

            Spacer(Modifier.height(12.dp))

            PremiumSettingCard(
                icon = Icons.Default.Lock,
                title = "Change Password",
                subtitle = "Update your account security"
            ) { showPasswordModal = true }

            Spacer(Modifier.height(32.dp))

            // ============================================================
            //  ABOUT SECTION
            // ============================================================
            SectionHeader("About")

            Spacer(Modifier.height(12.dp))

            PremiumSettingCard(
                icon = Icons.Default.Info,
                title = "Credits",
                subtitle = "Music & assets attribution"
            ) {
                navController.navigate("settings_credits")
            }
            Spacer(Modifier.height(12.dp))

            PremiumSettingCard(
                icon = Icons.Default.Info,
                title = "About Developer",
                subtitle = "Learn more about the developer"
            ) {
                navController.navigate("developer_page")
            }

            Spacer(Modifier.height(48.dp))

            // ============================================================
            //  LOG OUT BUTTON
            // ============================================================
            LogoutButton { showLogoutModal = true }

        }

        // ============================================================
        //  MODALS
        // ============================================================
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
                scope.launch {
                    UserPreferences.clearUser(context)
                    viewModel.logout {
                        successMessage = "Logged out successfully!"
                        navController.navigate("login_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        )

        // ============================================================
        //  SUCCESS TOAST
        // ============================================================
        successMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(1500)
                successMessage = null
            }
            SuccessToast(
                message = msg,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


//////////////////////////////////////////////////////////////////
//  PREMIUM PROFILE CARD
//////////////////////////////////////////////////////////////////

@Composable
fun PremiumProfileCard(username: String?) {
    val compliment = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning, ${username ?: "friend"} ✨"
            in 12..17 -> "Good afternoon, ${username ?: "friend"} 🌿"
            else -> "Good evening, ${username ?: "friend"} 🌙"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A1F1A),
                        Color(0xFF0F140F)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        BrandGreen.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar with premium green glow
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    BrandGreen.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    BrandGreen,
                                    BrandGreenBright
                                )
                            )
                        )
                        .border(3.dp, Color.Black.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username?.firstOrNull()?.uppercase() ?: "U",
                        style = AppTypography.displayLarge.copy(
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                username ?: "User",
                style = AppTypography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = AppWhite
            )

            Spacer(Modifier.height(8.dp))

            Text(
                compliment,
                style = AppTypography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = BrandGreen.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}


//////////////////////////////////////////////////////////////////
//  SECTION HEADER
//////////////////////////////////////////////////////////////////

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = AppTypography.labelMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = TextSecondary.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}


//////////////////////////////////////////////////////////////////
//  PREMIUM SETTING CARD
//////////////////////////////////////////////////////////////////

@Composable
fun PremiumSettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(100)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A1F1A),
                        Color(0xFF121712)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(20.dp)
            )
            .clickable {
                pressed = true
                onClick()
            }
            .padding(20.dp)
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon with green accent
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrandGreen.copy(alpha = 0.9f),
                                BrandGreenBright.copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = AppTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    ),
                    color = AppWhite
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    subtitle,
                    style = AppTypography.bodySmall.copy(
                        fontSize = 14.sp
                    ),
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.width(12.dp))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


//////////////////////////////////////////////////////////////////
//  LOGOUT BUTTON
//////////////////////////////////////////////////////////////////

@Composable
fun LogoutButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(100)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1F1414),
                        Color(0xFF170F0F)
                    )
                )
            )
            .border(
                1.dp,
                SystemStop.copy(alpha = 0.2f),
                RoundedCornerShape(20.dp)
            )
            .clickable {
                pressed = true
                onClick()
            }
            .padding(20.dp)
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = null,
                tint = SystemStop,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Log Out",
                color = SystemStop,
                style = AppTypography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            )
        }
    }
}