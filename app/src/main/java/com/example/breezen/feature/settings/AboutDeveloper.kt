package com.example.breezen.feature.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.breezen.R
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

// ----------------- Color Palette -----------------
private val LightGreen = Color(0xFFF1F8E9)
private val DarkGreen = Color(0xFF0F291E)
private val AccentGreen = Color(0xFF9CCC65)
private val YellowAccent = Color(0xFFFFF59D)
private val PureWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF546E7A)

// ----------------- Models -----------------
data class Language(val name: String, val color: Color)

val myLanguages = listOf(
    // Programming Languages
    Language("Java", Color(0xFFFFCCBC)),       // Soft Terracotta
    Language("C", Color(0xFFB0BEC5)),          // Mist Grey
    Language("C++", Color(0xFF90CAF9)),        // Pastel Blue
    Language("Python", Color(0xFFA5D6A7)),     // Soft Sage Green
    Language("JavaScript", Color(0xFFFFF59D)), // Your Theme's Yellow Accent
    Language("TypeScript", Color(0xFF9FA8DA)), // Periwinkle
    Language("SQL", Color(0xFFEF9A9A)),        // Soft Coral

    // Mobile
    Language("Kotlin", Color(0xFFCE93D8)),     // Pastel Lavender
    Language("Compose", Color(0xFF81D4FA)),    // Sky Blue

    // Web & DB
    Language("React", Color(0xFF80DEEA)),      // Soft Aqua
    Language("Next.js", Color(0xFF546E7A)),    // Soft Slate (Darker pastel for contrast)
    Language("Tailwind", Color(0xFF4DB6AC)),   // Muted Teal
    Language("Flask", Color(0xFFA1887F)),      // Earthy Brown
    Language("PostgresSQL", Color(0xFFC5E1A5)) // Matcha Green
)

data class Ball(
    val language: Language,
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    val radius: Float
)

@Composable
@Preview
fun DeveloperPreview(){
    DeveloperProfileScreen(navController = rememberNavController() )
}
// ----------------- Screen -----------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DeveloperProfileScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var showSkills by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(LightGreen, Color.White)))
    ) {

        // ================= HERO TOP ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Decorative Circle
            Box(
                modifier = Modifier
                    .offset((-60).dp, (-80).dp)
                    .size(300.dp)
                    .background(AccentGreen.copy(alpha = 0.1f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(44.dp)
                        .shadow(4.dp)
                        .background(DarkGreen, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = YellowAccent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 20.dp)
                            .zIndex(2f)
                    ) {
                        DecryptTextLine("ARYAN", 48, DarkGreen, FontWeight.Black)
                        DecryptTextLine("BALODI", 48, DarkGreen, FontWeight.Black)

                        // Role line
                        Spacer(modifier = Modifier.height(12.dp))

                        LoveBox()
                        Spacer(modifier = Modifier.height(12.dp))


                    }

                    Image(
                        painter = painterResource(id = R.drawable.developer_photo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit, // full image, no crop
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 80.dp ,y = -(50).dp)
                            .fillMaxHeight(.55f) // responsive height, 55% of screen
                            .wrapContentWidth()    // width adjusts automatically
                            .zIndex(1f)
                    )

                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .shadow(32.dp, RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .background(DarkGreen, RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- FIXED PILL AT TOP ---
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(PureWhite.copy(alpha = 0.2f), CircleShape)
            )

            Spacer(Modifier.height(16.dp))

            // --- EVERYTHING ELSE SPACED EVENLY ---
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = "\" Building seamless mobile experiences with code and coffee.\" ",
                    color = YellowAccent,
                    style = AppTypography.displayMedium,
                    fontFamily = FunnelDisplayFamily,
                    modifier = Modifier.fillMaxWidth()
                )

                ModernPillButton("WHAT I KNOW", YellowAccent, DarkGreen) {
                    showSkills = true
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SocialIconBtn(R.drawable.github, "GitHub") {
                        openUrl(navController.context, "https://github.com")
                    }
                    SocialIconBtn(R.drawable.linkedin, "LinkedIn") {
                        openUrl(navController.context, "https://linkedin.com")
                    }
                    SocialIconBtn(R.drawable.gmail, "Email") {
                        openEmail(navController.context)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernActionButton(
                        "Feedback",
                        Icons.AutoMirrored.Rounded.Message,
                        false,
                        Modifier.weight(1f)
                    ) { showFeedbackDialog = true }

                    ModernActionButton(
                        "Buy Coffee",
                        Icons.Rounded.Coffee,
                        true,
                        Modifier.weight(1f)
                    ) { showDonateDialog = true }
                }
            }
        }

        // ================ DIALOGS ===================
        if (showFeedbackDialog) {
            ModalOverlay {
                FeedbackDialog(
                    viewModel = viewModel,
                    onDismiss = { showFeedbackDialog = false }
                )
            }
        }

        if (showDonateDialog) {
            ModalOverlay {
                ModernDialog(
                    title = "Buy me a Coffee",
                    message = "Thanks for supporting the work!",
                    btnText = "Support",
                    onDismiss = { showDonateDialog = false }
                )
            }
        }

        if (showSkills) {
            PhysicsBallsOverlay(myLanguages) { showSkills = false }
        }
    }
}

@Composable
fun LoveBox() {
    // One-word, inspiring, professional words
    val items = listOf(
        "Creating",
        "Learning",
        "Building",
        "Improving",
        "Designing",
        "Innovating",
        "Coding",
        "Exploring",
        "Dreaming",
        "Growing",
        "Inspiring",
        "Inventing"
    )

    var index by remember { mutableStateOf(0) }

    // Auto rotate every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            index = (index + 1) % items.size
        }
    }

    Box(


    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = "I love",
                color = DarkGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(    modifier = Modifier

                .clip(RoundedCornerShape(7.dp))
                .background(DarkGreen)
                .padding(horizontal = 9.dp, vertical = 6.dp)){
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    }
                ) { i ->
                    Text(
                        text = items[i],
                        color = YellowAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,

                        )
                }
            }
        }
    }
}


// ------------------- MODALS ----------------------------

@Composable
fun FeedbackDialog(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .width(330.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(DarkGreen)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Send Feedback", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = YellowAccent)
        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Write your feedback…", color = PureWhite.copy(0.5f)) },
            textStyle = LocalTextStyle.current.copy(color = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1C3A2A),
                unfocusedContainerColor = Color(0xFF1C3A2A),
                cursorColor = YellowAccent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(18.dp))


        Button(
            onClick = {
                if (text.isBlank()) return@Button

                sending = true
                scope.launch {
                    val result = viewModel.sendUserFeedback(text)
                    sending = false
                    sent = result
                    delay(1400)
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = YellowAccent)
        ) {
            Text(
                when {
                    sending -> "Sending…"
                    sent -> "Sent!"
                    else -> "Send"
                },
                color = DarkGreen
            )
        }


        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Cancel",
            color = PureWhite.copy(0.8f),
            modifier = Modifier.clickable { onDismiss() }
        )
    }
}

@Composable
fun ModernDialog(title: String, message: String, btnText: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .width(320.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkGreen)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = YellowAccent)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = PureWhite, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = YellowAccent)
        ) { Text(btnText, color = DarkGreen) }
    }
}

@Composable
fun ModalOverlay(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// --------------- Buttons & Icons --------------------

@Composable
fun SocialIconBtn(icon: Int, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.bounceClick().clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(PureWhite.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(icon), null, tint = YellowAccent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = PureWhite.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
fun ModernPillButton(text: String, backgroundColor: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .bounceClick()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun ModernActionButton(
    text: String,
    icon: ImageVector,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isPrimary) AccentGreen else Color(0xFF1B3829)
    val contentColor = if (isPrimary) DarkGreen else PureWhite.copy(alpha = 0.9f)

    Row(
        modifier = modifier
            .height(64.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .bounceClick()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// ----------------- Decrypt Text Effect -----------------

@Composable
fun DecryptTextLine(text: String, fontSize: Int, color: Color, fontWeight: FontWeight) {
    var display by remember { mutableStateOf("") }
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789*&^%"

    LaunchedEffect(text) {
        display = text.map { chars.random() }.joinToString("")
        for (i in text.indices) {
            delay(100)
            display = display.take(i) + text[i] +
                    display.substring(i + 1)
                        .map { chars.random() }
                        .joinToString("")
        }
        display = text
    }

    Text(
        display,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        letterSpacing = (-1).sp
    )
}

// ----------------- OPTIMIZED PHYSICS OVERLAY -----------------

@Composable
fun PhysicsBallsOverlay(languages: List<Language>, onDismiss: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Transparent)
    ) {
        val width = maxWidth.value
        val height = maxHeight.value

        // DYNAMIC SIZING: Calculate radius based on screen width
        // Balls will be between 11% and 16% of the screen width
        val minRadius = width * 0.11f
        val maxRadius = width * 0.16f

        val balls = remember {
            languages.mapIndexed { i, lang ->
                Ball(
                    lang,
                    x = width / 2 + (Random.nextFloat() - 0.5f) * (width * 0.5f),
                    y = -height * 0.5f - i * (maxRadius * 2), // Drop them from high up
                    vx = (Random.nextFloat() - 0.5f) * 50f,
                    vy = 0f,
                    radius = minRadius + Random.nextFloat() * (maxRadius - minRadius)
                )
            }.toMutableList()
        }

        var trigger by remember { mutableLongStateOf(0L) }

        LaunchedEffect(Unit) {
            val gravity = 1500f // Stronger gravity for "heavy" feel
            val drag = 0.99f
            val dt = 0.016f

            while (isActive) {
                // 1. Move
                balls.forEach { b ->
                    b.vy += gravity * dt
                    b.vx *= drag
                    b.vy *= drag
                    b.x += b.vx * dt
                    b.y += b.vy * dt
                }

                // 2. Solve Collisions (Repeat for stability)
                repeat(4) {
                    // Wall Interactions
                    balls.forEach { b ->
                        // Floor
                        if (b.y > height - b.radius) {
                            b.y = height - b.radius
                            b.vy *= -0.5f // Dampened bounce
                            b.vx *= 0.9f  // Floor friction
                        }
                        // Ceiling (optional, prevents flying off top)
                        if (b.y < -height * 2) {
                            // If it flies WAY too high, push it down (safety net)
                            b.y = -height.toFloat()
                        }

                        // Walls
                        if (b.x < b.radius) {
                            b.x = b.radius
                            b.vx *= -0.6f
                        }
                        if (b.x > width - b.radius) {
                            b.x = width - b.radius
                            b.vx *= -0.6f
                        }
                    }

                    // Ball-to-Ball Interactions
                    for (i in balls.indices) {
                        for (j in i + 1 until balls.size) {
                            val b1 = balls[i]
                            val b2 = balls[j]
                            val dx = b2.x - b1.x
                            val dy = b2.y - b1.y
                            val distSq = dx * dx + dy * dy
                            val minDist = b1.radius + b2.radius

                            if (distSq < minDist * minDist) {
                                val dist = sqrt(distSq)
                                val currentDist = if (dist == 0f) 0.1f else dist
                                val nx = dx / currentDist
                                val ny = dy / currentDist
                                val overlap = minDist - currentDist

                                // Separate
                                b1.x -= nx * overlap * 0.5f
                                b1.y -= ny * overlap * 0.5f
                                b2.x += nx * overlap * 0.5f
                                b2.y += ny * overlap * 0.5f

                                // Exchange Momentum
                                val rvx = b2.vx - b1.vx
                                val rvy = b2.vy - b1.vy
                                val velAlongNormal = rvx * nx + rvy * ny

                                if (velAlongNormal < 0) {
                                    val restitution = 0.7f
                                    val impulse = -(1 + restitution) * velAlongNormal
                                    val impulseScale = impulse * 0.5f
                                    b1.vx -= nx * impulseScale
                                    b1.vy -= ny * impulseScale
                                    b2.vx += nx * impulseScale
                                    b2.vy += ny * impulseScale
                                }
                            }
                        }
                    }
                }

                // 3. Final Clamp (Strict Overflow Protection)
                // Even if physics pushes them out, this forces them back in.
                balls.forEach { b ->
                    b.x = b.x.coerceIn(b.radius, width - b.radius)
                    b.y = b.y.coerceAtMost(height - b.radius)
                }

                trigger++
                delay(16)
            }
        }

        Box(Modifier.fillMaxSize()) {
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Draw Balls
            balls.forEach { b ->
                key(b.language.name, trigger) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            // Optimized rendering
                            .graphicsLayer {
                                translationX = (b.x - b.radius).dp.toPx()
                                translationY = (b.y - b.radius).dp.toPx()
                            }
                            .size((b.radius * 2).dp)
                            .shadow(6.dp, CircleShape)
                            .background(b.language.color, CircleShape)
                            .clickable {
                                // Fun interaction: Kick the ball
                                b.vy = -1200f
                                b.vx = (Random.nextFloat() - 0.5f) * 1000f
                            }
                    ) {
                        // Responsive Text Sizing
                        val isDark = b.language.name in listOf("NumPy", "Next.js", "C", "Flask")
                        Text(
                            b.language.name,
                            fontSize = (b.radius * 0.35f).sp, // Text scales with ball
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) PureWhite else Color(0xFF0F291E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
// ----------------- Utils ------------------------
fun Modifier.bounceClick() = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "scale")
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitFirstDown(false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
    }
}

fun openEmail(context: Context) {
    context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:your@email.com".toUri()))
}

fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}