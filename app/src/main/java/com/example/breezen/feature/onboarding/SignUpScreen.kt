package com.example.breezen.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breezen.R
import com.example.breezen.core.data.UserPreferences
import com.example.breezen.core.network.AuthService
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.Prata
import com.example.breezen.core.ui.theme.SystemWarning
import kotlinx.coroutines.launch

// ----------------------------------------------------------
// Password Strength Evaluation
// ----------------------------------------------------------
private enum class PasswordStrength { NONE, WEAK, MEDIUM, STRONG }

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

// ----------------------------------------------------------
// Sign Up Screen
// ----------------------------------------------------------
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
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val strength by remember { derivedStateOf { calculatePasswordStrength(password) } }

    LaunchedEffect(Unit) {
        visible = true
    }

    // TextField Color Configuration
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

        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,

        cursorColor = MaterialTheme.colorScheme.onBackground,
        errorTextColor = MaterialTheme.colorScheme.error,
        errorCursorColor = MaterialTheme.colorScheme.error
    )

    fun enterAnimation(delay: Int) =
        slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(800, delay)) +
                fadeIn(tween(800, delay))

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


            Image(
                painter = painterResource(R.drawable.logo_without_background),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                AnimatedVisibility(visible = visible, enter = enterAnimation(0)) {
                    Text(
                        text = "Create Your Account",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        fontFamily = FunnelDisplayFamily
                    )
                }

                AnimatedVisibility(visible = visible, enter = enterAnimation(100)) {
                    Text(
                        text = "Begin your journey to a calmer mind.",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Prata,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(48.dp))

                // --------------------------
                // Name Field
                // --------------------------
                AnimatedVisibility(visible = visible, enter = enterAnimation(200)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", style = AppTypography.bodySmall.copy(fontFamily = FunnelDisplayFamily)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        readOnly = isLoading
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --------------------------
                // Email Field
                // --------------------------
                AnimatedVisibility(visible = visible, enter = enterAnimation(300)) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email" , style = AppTypography.bodySmall.copy(fontFamily = FunnelDisplayFamily)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        readOnly = isLoading
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --------------------------
                // Password Field
                // --------------------------
                AnimatedVisibility(visible = visible, enter = enterAnimation(400)) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password" , style = AppTypography.bodySmall.copy(fontFamily = FunnelDisplayFamily)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        readOnly = isLoading
                    )
                }

                // --------------------------
                // Password Strength Bar
                // --------------------------
                AnimatedVisibility(visible = visible, enter = enterAnimation(450)) {
                    PasswordStrengthIndicator(
                        strength = strength,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }

                // --------------------------
                // Error Message
                // --------------------------
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // --------------------------
                // Sign Up Button
                // --------------------------
                AnimatedVisibility(visible = visible, enter = enterAnimation(500)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    AuthService.signUp(name, email, password)
                                    UserPreferences.saveUsername(context, name)
                                    onSignUpSuccess()

                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Unknown error."
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDarker),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = AppWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                color = AppWhite,
                                style = AppTypography.bodyMedium.copy(fontFamily = FunnelDisplayFamily)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --------------------------
            // Already Have Account Button (white text)
            // --------------------------
            AnimatedVisibility(visible = visible, enter = enterAnimation(600)) {
                OutlinedButton(
                    onClick = onSignInClick,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, AppWhite)
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        color = AppWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }



        }
    }
}

// ----------------------------------------------------------
// Password Strength Indicator Bar
// ----------------------------------------------------------
@Composable
private fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {

    val weakColor = MaterialTheme.colorScheme.error
    val mediumColor = SystemWarning
    val strongColor = MaterialTheme.colorScheme.primary
    val defaultColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)

    val bar1 by animateColorAsState(
        if (strength != PasswordStrength.NONE) weakColor else defaultColor,
        tween(300)
    )
    val bar2 by animateColorAsState(
        if (strength == PasswordStrength.MEDIUM || strength == PasswordStrength.STRONG) mediumColor else defaultColor,
        tween(300)
    )
    val bar3 by animateColorAsState(
        if (strength == PasswordStrength.STRONG) strongColor else defaultColor,
        tween(300)
    )

    Row(
        modifier = modifier.height(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(bar1)
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(bar2)
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(bar3)
        )
    }
}
