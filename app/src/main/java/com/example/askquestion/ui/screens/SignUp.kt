package com.example.askquestion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// Password strength logic
private enum class PasswordStrength {
    NONE, WEAK, MEDIUM, STRONG
}

private fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    return when {
        score >= 4 -> PasswordStrength.STRONG
        score == 3 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.WEAK
    }
}

@Composable
fun SignUpScreen(
    onSignInClick: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val randomQuote = remember { QuoteProvider.getRandomQuote() }
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val strength by remember {
        derivedStateOf { calculatePasswordStrength(password) }
    }

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

    fun enterAnimation(delay: Int) = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(durationMillis = 800, delayMillis = delay)
    ) + fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = delay))

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
                AnimatedVisibility(visible = visible, enter = enterAnimation(0)) {
                    Text(
                        text = "Create Your Account",
                        color = Color.White.copy(alpha = 0.95f), // *** UPDATED: Softer white
                        style = CustomTypography.displayLarge.copy(fontSize = 32.sp),
                    )
                }

                AnimatedVisibility(visible = visible, enter = enterAnimation(100)) {
                    Text(
                        text = "Begin your journey to a calmer mind.",
                        color = Color.White.copy(alpha = 0.7f), // This is already calm
                        style = CustomTypography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                AnimatedVisibility(visible = visible, enter = enterAnimation(200)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Name Icon"
                            )
                        },
                        readOnly = isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = visible, enter = enterAnimation(300)) {
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

                AnimatedVisibility(visible = visible, enter = enterAnimation(400)) {
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



                AnimatedVisibility(visible = visible, enter = enterAnimation(450)) {
                    PasswordStrengthIndicator(
                        strength = strength,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 4.dp, end = 4.dp)
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

                AnimatedVisibility(visible = visible, enter = enterAnimation(500)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    AuthService.signUp(name, email, password)
                                    onSignUpSuccess()
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
                                    text = "Sign Up",
                                    color = Color.White, // *** UPDATED: White text on new button
                                    style = CustomTypography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = visible, enter = enterAnimation(600)) {
                OutlinedButton(
                    onClick = { onSignInClick() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    // *** UPDATED: Softer light green accent
                    border = BorderStroke(1.dp, Color(0xFF69F0AE))
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        color = Color(0xFF69F0AE), // *** UPDATED: Softer light green
                        style = CustomTypography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = visible, enter = enterAnimation(700)) {
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

@Composable
private fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val weakColor = Color(0xFFF48FB1)
    val mediumColor = Color(0xFFFFD54F)
    val strongColor = Color(0xFF69F0AE) // *** UPDATED: Softer light green
    val defaultColor = Color.White.copy(alpha = 0.3f)

    val bar1Color by animateColorAsState(
        targetValue = if (strength != PasswordStrength.NONE) weakColor else defaultColor,
        animationSpec = tween(300), label = "bar1"
    )
    val bar2Color by animateColorAsState(
        targetValue = if (strength == PasswordStrength.MEDIUM || strength == PasswordStrength.STRONG) mediumColor else defaultColor,
        animationSpec = tween(300), label = "bar2"
    )
    val bar3Color by animateColorAsState(
        targetValue = if (strength == PasswordStrength.STRONG) strongColor else defaultColor,
        animationSpec = tween(300), label = "bar3"
    )

    Row(
        modifier = modifier.height(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(bar1Color)
        )
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(bar2Color)
        )
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(bar3Color)
        )
    }
}