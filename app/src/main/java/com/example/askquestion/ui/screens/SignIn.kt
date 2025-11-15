package com.example.askquestion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.askquestion.network.AuthService
import com.example.askquestion.theme.CustomTypography
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val randomQuote = remember { QuoteProvider.getRandomQuote() }
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color(0xFF69F0AE), // Soft light green accent
        focusedLabelColor = Color(0xFF69F0AE),
        focusedLeadingIconColor = Color(0xFF69F0AE),
        unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White.copy(alpha = 0.7f), // *** NEW: Softer unfocused text
        cursorColor = Color.White,
        errorTextColor = Color(0xFFF48FB1), // Soft Red
        errorCursorColor = Color(0xFFF48FB1)
    )

    // Animation specs
    val enterAnimationBottom = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(durationMillis = 700, delayMillis = 100)
    ) + fadeIn(animationSpec = tween(durationMillis = 700, delayMillis = 100))

    val enterAnimationLeft = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(durationMillis = 700, delayMillis = 100)
    ) + fadeIn(animationSpec = tween(durationMillis = 700, delayMillis = 100))

    val enterAnimationRight = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = 700, delayMillis = 100)
    ) + fadeIn(animationSpec = tween(durationMillis = 700, delayMillis = 100))

    val enterAnimationFade = fadeIn(animationSpec = tween(durationMillis = 800))

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Layer 1: The background
        BreathingOrbBackground()

        // Layer 2: The UI content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(visible = visible, enter = enterAnimationFade) {
                    Text(
                        text = "Welcome Back",
                        color = Color.White.copy(alpha = 0.95f), // *** UPDATED: Softer white
                        style = CustomTypography.displayLarge.copy(fontSize = 32.sp),
                    )
                }

                AnimatedVisibility(visible = visible, enter = enterAnimationFade) {
                    Text(
                        text = "Enter your details to continue your journey.",
                        color = Color.White.copy(alpha = 0.7f), // This is already calm
                        style = CustomTypography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                AnimatedVisibility(visible = visible, enter = enterAnimationLeft) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        readOnly = isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = visible, enter = enterAnimationRight) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Password Icon"
                            )
                        },
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff
                            val description =
                                if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    description,
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        readOnly = isLoading
                    )
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFF48FB1), // Soft red
                            style = CustomTypography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(visible = visible, enter = enterAnimationBottom) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    AuthService.signIn(email, password)
                                    onSignInSuccess()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "An unknown error occurred."
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        // *** UPDATED: New deep, calm green button
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)),
                        enabled = !isLoading
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.Black, // Text on this button is black
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    color = Color.White, // *** UPDATED: White text on new button
                                    style = CustomTypography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = visible, enter = enterAnimationBottom) {
                OutlinedButton(
                    onClick = { onSignUpClick() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    // *** UPDATED: Softer light green accent
                    border = BorderStroke(1.dp, Color(0xFF69F0AE))
                ) {
                    Text(
                        text = "Don't have an account? Sign Up",
                        color = Color(0xFF69F0AE), // *** UPDATED: Softer light green
                        style = CustomTypography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = visible, enter = enterAnimationFade) {
                Text(
                    text = "\u201C${randomQuote}\u201D",
                    color = Color.White.copy(alpha = 0.7f),
                    style = CustomTypography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


object QuoteProvider {
    private val quotes = listOf(
        "The mind is everything. What you think, you become.",
        "Peace comes from within. Do not seek it without.",
        "Quiet the mind, and the soul will speak.",
        "The present moment is filled with joy and happiness. If you are attentive, you will see it.",
        "Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure."
    )

    fun getRandomQuote(): String {
        return quotes[Random.nextInt(0, quotes.size)]
    }
}