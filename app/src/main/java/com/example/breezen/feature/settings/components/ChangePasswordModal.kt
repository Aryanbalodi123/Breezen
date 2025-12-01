package com.example.breezen.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.BrandGreenBright
import com.example.breezen.core.ui.theme.GlassBackground
import com.example.breezen.core.ui.theme.GlassBorder
import com.example.breezen.core.ui.theme.SystemStop
import com.example.breezen.core.ui.theme.SystemWarning
import com.example.breezen.core.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    if (!visible) return

    val vmState by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    val strength = remember(newPass) { calculatePasswordStrength(newPass) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(AppBlack.copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER --------------------------------------------------
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(42.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text("Change Password",
                style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = AppWhite
            )

            Text(
                "Secure your account with a new password",
                style = AppTypography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            // INPUT FIELDS -------------------------------------------
            FancyPasswordField(
                label = "Current Password",
                value = oldPass,
                onValueChange = { oldPass = it; errorMsg = null },
                show = showOld,
                onToggle = { showOld = !showOld },
                isError = errorMsg != null && oldPass.isBlank()
            )

            Spacer(Modifier.height(14.dp))

            FancyPasswordField(
                label = "New Password",
                value = newPass,
                onValueChange = { newPass = it; errorMsg = null },
                show = showNew,
                onToggle = { showNew = !showNew },
                isError = errorMsg != null && newPass.length < 6
            )

            Spacer(Modifier.height(14.dp))

            FancyPasswordField(
                label = "Confirm Password",
                value = confirmPass,
                onValueChange = { confirmPass = it; errorMsg = null },
                show = showConfirm,
                onToggle = { showConfirm = !showConfirm },
                isError = errorMsg != null && newPass != confirmPass
            )

            Spacer(Modifier.height(20.dp))

            // STRENGTH BAR -------------------------------------------
            AnimatedVisibility(
                visible = newPass.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                EnhancedPasswordStrengthBar(strength, newPass)
            }

            Spacer(Modifier.height(18.dp))

            // ERROR BOX ----------------------------------------------
            AnimatedVisibility(
                visible = errorMsg != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ErrorCard(errorMsg ?: "")
            }

            Spacer(Modifier.height(24.dp))

            // BUTTONS -------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallButton(
                    text = "Cancel",
                    bg = Color.Transparent,
                    border = Color.White.copy(alpha = 0.2f),
                    textColor = AppWhite
                ) {
                    onDismissRequest()
                }

                SmallButton(
                    text = "Update",
                    bg = BrandGreen,
                    textColor = AppBlack,
                    loading = vmState.isLoading
                ) {
                    when {
                        oldPass.isBlank() -> errorMsg = "Enter your current password"
                        newPass.length < 6 -> errorMsg = "Password must be at least 6 characters"
                        newPass != confirmPass -> errorMsg = "Passwords do not match"
                        else -> {
                            scope.launch {
                                val res = viewModel.securePasswordUpdate(oldPass, newPass, context)
                                if (res == null) onDismissRequest()
                                else errorMsg = res
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun FancyPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    show: Boolean,
    onToggle: () -> Unit,
    isError: Boolean
) {
    var focused by remember { mutableStateOf(false) }

    val glow by animateFloatAsState(
        targetValue = if (focused) 1f else 0.2f,
        animationSpec = tween(240),
        label = ""
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111))
            .border(
                1.dp,
                when {
                    isError -> SystemStop
                    else -> BrandGreen.copy(alpha = glow)
                },
                RoundedCornerShape(16.dp)
            ),
        label = {
            Text(
                label,
                color = if (isError) SystemStop else TextSecondary
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    tint = if (isError) SystemStop else TextSecondary,
                    contentDescription = null
                )
            }
        },
        singleLine = true,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = BrandGreen,
            focusedLabelColor = BrandGreen,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = AppWhite,
            unfocusedTextColor = AppWhite
        ),
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}
@Composable
fun RowScope.SmallButton(
    text: String,
    bg: Color,
    textColor: Color,
    border: Color = Color.Transparent,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        modifier = Modifier
            .height(50.dp)
            .weight(1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = textColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text,
                color = textColor,
                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}


@Composable
fun ErrorCard(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SystemStop.copy(alpha = 0.14f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SystemStop)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                msg,
                color = SystemStop,
                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}



@Composable
fun EnhancedPasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    show: Boolean,
    onToggle: () -> Unit,
    isError: Boolean = false
) {
    val borderColor = when {
        isError -> SystemStop
        value.isNotEmpty() -> BrandGreen
        else -> GlassBorder
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = if (isError) SystemStop else TextSecondary,
                style = AppTypography.bodyMedium
            )
        },
        singleLine = true,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (show) "Hide password" else "Show password",
                    tint = if (isError) SystemStop else TextSecondary
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) SystemStop else BrandGreen,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = GlassBackground.copy(alpha = 0.5f),
            unfocusedContainerColor = GlassBackground.copy(alpha = 0.3f),
            cursorColor = BrandGreen,
            focusedLabelColor = if (isError) SystemStop else BrandGreen,
            unfocusedLabelColor = if (isError) SystemStop else TextSecondary,
            focusedTextColor = AppWhite,
            unfocusedTextColor = AppWhite,
            errorBorderColor = SystemStop,
            errorLabelColor = SystemStop
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        isError = isError
    )
}

@Composable
fun EnhancedPasswordStrengthBar(strength: Float, password: String) {
    val animatedStrength by animateFloatAsState(
        targetValue = strength,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "strength"
    )

    val strengthLevel = when {
        strength < 0.3f -> "Weak"
        strength < 0.6f -> "Fair"
        strength < 0.8f -> "Good"
        else -> "Strong"
    }

    val strengthColor = when {
        strength < 0.3f -> SystemStop
        strength < 0.6f -> SystemWarning
        strength < 0.8f -> BrandGreen
        else -> BrandGreenBright
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Password Strength",
                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = strength >= 0.8f,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BrandGreenBright,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                }
                Text(
                    strengthLevel,
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = strengthColor
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassBorder.copy(alpha = 0.3f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedStrength)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                strengthColor.copy(alpha = 0.8f),
                                strengthColor
                            )
                        )
                    )
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        // Password requirements
        AnimatedVisibility(
            visible = password.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(Modifier.padding(top = 12.dp)) {
                PasswordRequirement("At least 6 characters", password.length >= 6)
                PasswordRequirement("Contains numbers", password.any { it.isDigit() })
                PasswordRequirement("Contains uppercase", password.any { it.isUpperCase() })
                PasswordRequirement("Contains special char", password.any { !it.isLetterOrDigit() })
            }
        }
    }
}

@Composable
fun PasswordRequirement(text: String, met: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (met) 1f else 0.5f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (met) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 3.dp)
            .alpha(alpha)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (met) BrandGreenBright else TextSecondary.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = AppTypography.bodySmall,
            color = if (met) BrandGreenBright else TextSecondary,
            fontWeight = if (met) FontWeight.Medium else FontWeight.Normal
        )
    }
}

private fun calculatePasswordStrength(password: String): Float {
    var score = 0f
    if (password.length >= 6) score += 0.25f
    if (password.length >= 10) score += 0.15f
    if (password.any { it.isDigit() }) score += 0.2f
    if (password.any { it.isUpperCase() }) score += 0.2f
    if (password.any { it.isLowerCase() }) score += 0.1f
    if (password.any { !it.isLetterOrDigit() }) score += 0.2f
    return score.coerceIn(0f, 1f)
}