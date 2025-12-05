package com.example.breezen.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.breezen.R
import com.example.breezen.core.data.UserPreferences
import com.example.breezen.core.network.AuthService
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.SystemError
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    onSignInSuccess: () -> Unit
) {

    // ---- UI State ----
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Trigger entrance animations
    LaunchedEffect(Unit) { visible = true }

    // ---- Field Colors (unchanged logic) ----
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
        unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
        disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),

        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),

        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,

        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,

        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,

        cursorColor = TextPrimary,
        errorTextColor = SystemError,
        errorCursorColor = SystemError
    )

    // ---- Animation Specs ----
    val enterBottom = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(700)) +
            fadeIn(tween(700))
    val enterLeft = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(700)) +
            fadeIn(tween(700))
    val enterRight = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(700)) +
            fadeIn(tween(700))
    val enterFade = fadeIn(tween(800))

    Box(Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Image(
                    painter = painterResource(R.drawable.logo_without_background),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(visible = visible, enter = enterFade) {
                    Text(
                        text = "Welcome Back",
                        color = TextPrimary,
                        style = AppTypography.displayLarge,
                        fontFamily = FunnelDisplayFamily
                    )
                }

                AnimatedVisibility(visible = visible, enter = enterFade) {
                    Text(
                        text = "Enter your details to continue your journey.",
                        color = TextSecondary,
                        style = AppTypography.bodyMedium,
                        fontFamily = FunnelDisplayFamily
                    )
                }

                Spacer(Modifier.height(48.dp))

                // ---- Email Field ----
                AnimatedVisibility(visible = visible, enter = enterLeft) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", style = AppTypography.bodySmall.copy(fontFamily = FunnelDisplayFamily)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null)
                        },
                        readOnly = isLoading
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ---- Password Field ----
                AnimatedVisibility(visible = visible, enter = enterRight) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", style = AppTypography.bodySmall.copy(fontFamily = FunnelDisplayFamily)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextPrimary
                                )
                            }
                        },
                        readOnly = isLoading
                    )
                }

                // ---- Error Message ----
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = SystemError,
                            style = AppTypography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ---- Sign In Button ----
                AnimatedVisibility(visible = visible, enter = enterBottom) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null

                                try {
                                    AuthService.signIn(email, password)
                                    val user = AuthService.getCurrentUser()

                                    user?.username?.let { saved ->
                                        UserPreferences.saveUsername(context, saved)
                                    }

                                    onSignInSuccess()

                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Something went wrong."
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDarker)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                color = TextPrimary,
                                style = AppTypography.bodyLarge.copy(fontFamily = FunnelDisplayFamily)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = visible, enter = enterBottom) {
                OutlinedButton(
                    onClick = onSignUpClick,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, AppWhite)
                ) {
                    Text(
                        text = "Don't have an account? Sign Up",
                        color = TextPrimary,
                        style = AppTypography.bodyMedium.copy(fontFamily = FunnelDisplayFamily)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))



        }
    }
}
