package com.example.breezen.feature.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.breezen.core.ui.theme.AccentGreen
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.DarkGreen
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.LightGreen
import com.example.breezen.core.ui.theme.PureWhite
import com.example.breezen.core.ui.theme.YellowAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

data class Language(val name: String, val color: Color)

val myLanguages = listOf(
    Language("Java", Color(0xFFFFCCBC)),
    Language("C", Color(0xFFB0BEC5)),
    Language("C++", Color(0xFF90CAF9)),
    Language("Python", Color(0xFFA5D6A7)),
    Language("JavaScript", Color(0xFFFFF59D)),
    Language("TypeScript", Color(0xFF9FA8DA)),
    Language("SQL", Color(0xFFEF9A9A)),
    Language("Kotlin", Color(0xFFCE93D8)),
    Language("Compose", Color(0xFF81D4FA)),
    Language("React", Color(0xFF80DEEA)),
    Language("Next.js", Color(0xFF546E7A)),
    Language("Tailwind", Color(0xFF4DB6AC)),
    Language("Flask", Color(0xFFA1887F)),
    Language("PostgresSQL", Color(0xFFC5E1A5))
)

data class Ball(
    val language: Language,
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    val radius: Float
)

@Preview
@Composable
fun DeveloperPreview() {
    DeveloperProfileScreen(rememberNavController())
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DeveloperProfileScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var showSkills by remember { mutableStateOf(false) }

    // Professional entrance animations
    val backgroundAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(-40f) }
    val imageScale = remember { Animatable(0.85f) }
    val bottomSheetOffsetY = remember { Animatable(300f) }
    val decorCircleScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            backgroundAlpha.animateTo(1f, tween(350))
        }
        launch {
            delay(80)
            decorCircleScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(120)
            headerOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            delay(160)
            imageScale.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        }
        launch {
            delay(240)
            bottomSheetOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            delay(400)
            contentAlpha.animateTo(1f, tween(450))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = backgroundAlpha.value
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(LightGreen, Color.White)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .offset((-60).dp, (-80).dp)
                    .size(300.dp)
                    .graphicsLayer {
                        scaleX = decorCircleScale.value
                        scaleY = decorCircleScale.value
                    }
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
                        .graphicsLayer {
                            alpha = contentAlpha.value
                        }
                        .shadow(4.dp)
                        .background(DarkGreen, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = YellowAccent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer {
                                translationY = headerOffsetY.value
                                alpha = if (headerOffsetY.value > -20f) 1f else 0f
                            }
                            .padding(top = 20.dp)
                            .zIndex(2f)
                    ) {
                        ConsoleRevealEffect("ARYAN", 48, DarkGreen, FontWeight.Black)
                        ConsoleRevealEffect("BALODI", 48, DarkGreen, FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfiniteLoopText()
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Image(
                        painter = painterResource(id = R.drawable.developer_photo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 80.dp, y = (-50).dp)
                            .fillMaxHeight(.55f)
                            .wrapContentWidth()
                            .graphicsLayer {
                                scaleX = imageScale.value
                                scaleY = imageScale.value
                            }
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
                .graphicsLayer {
                    translationY = bottomSheetOffsetY.value
                }
                .shadow(32.dp, RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .background(DarkGreen, RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(PureWhite.copy(alpha = 0.2f), CircleShape)
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = contentAlpha.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "\" Building seamless mobile experiences with code and coffee.\" ",
                    color = YellowAccent,
                    style = AppTypography.displayMedium,
                    fontFamily = FunnelDisplayFamily
                )

                SkillNodeTrigger("WHAT I KNOW", YellowAccent, DarkGreen) {
                    showSkills = true
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RepoLinkButton(R.drawable.github, "GitHub") {
                        openUrl(navController.context, "https://github.com/Aryanbalodi123")
                    }
                    RepoLinkButton(R.drawable.linkedin, "LinkedIn") {
                        openUrl(navController.context, "https://www.linkedin.com/in/aryan-balodi-522a6334b/")
                    }
                    RepoLinkButton(R.drawable.gmail, "Email") {
                        openEmail(navController.context)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionCard(
                        "Feedback",
                        Icons.AutoMirrored.Rounded.Message,
                        false,
                        Modifier.weight(1f)
                    ) { showFeedbackDialog = true }

                    DashboardActionCard(
                        "Buy Coffee",
                        Icons.Rounded.Coffee,
                        true,
                        Modifier.weight(1f)
                    ) { showDonateDialog = true }
                }
            }
        }

        if (showFeedbackDialog) {
            ModalOverlay {
                FeedbackDialog(viewModel) { showFeedbackDialog = false }
            }
        }

        if (showDonateDialog) {
            ModalOverlay {
                SystemOverlayDialog(
                    "Buy me a Coffee",
                    "Thanks for supporting the work!",
                    "Support"
                ) { showDonateDialog = false }
            }
        }

        if (showSkills) {
            PhysicsEngineOverlay(myLanguages) { showSkills = false }
        }
    }
}

@Composable
fun InfiniteLoopText() {
    val items = listOf(
        "Creating", "Learning", "Building", "Improving",
        "Designing", "Innovating", "Coding", "Exploring",
        "Dreaming", "Growing", "Inspiring", "Inventing"
    )
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            index = (index + 1) % items.size
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("I love", color = DarkGreen, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(DarkGreen)
                .padding(horizontal = 9.dp, vertical = 6.dp)
        ) {
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                },
                label = "loop"
            ) {
                Text(items[it], color = YellowAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FeedbackDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dialogScale = remember { Animatable(0.85f) }
    val dialogAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { dialogScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch { dialogAlpha.animateTo(1f, tween(250)) }
    }

    Column(
        modifier = Modifier
            .width(330.dp)
            .graphicsLayer {
                scaleX = dialogScale.value
                scaleY = dialogScale.value
                alpha = dialogAlpha.value
            }
            .clip(RoundedCornerShape(28.dp))
            .background(DarkGreen)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Send Feedback", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = YellowAccent)
        Spacer(Modifier.height(12.dp))
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Write your feedback…", color = PureWhite.copy(0.5f)) },
            textStyle = LocalTextStyle.current.copy(color = PureWhite),
            modifier = Modifier.fillMaxWidth().height(140.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1C3A2A),
                unfocusedContainerColor = Color(0xFF1C3A2A),
                cursorColor = YellowAccent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (text.isBlank()) return@Button
                sending = true
                scope.launch {
                    viewModel.sendUserFeedback(text)
                    delay(1200)
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = YellowAccent)
        ) {
            Text(if (sending) "Sending…" else "Send", color = DarkGreen)
        }
    }
}

@Composable
fun SystemOverlayDialog(title: String, message: String, btnText: String, onDismiss: () -> Unit) {
    val dialogScale = remember { Animatable(0.85f) }
    val dialogAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { dialogScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch { dialogAlpha.animateTo(1f, tween(250)) }
    }

    Column(
        modifier = Modifier
            .width(320.dp)
            .graphicsLayer {
                scaleX = dialogScale.value
                scaleY = dialogScale.value
                alpha = dialogAlpha.value
            }
            .clip(RoundedCornerShape(24.dp))
            .background(DarkGreen)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = YellowAccent)
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = PureWhite, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = YellowAccent)
        ) {
            Text(btnText, color = DarkGreen)
        }
    }
}

@Composable
fun ModalOverlay(content: @Composable () -> Unit) {
    val overlayAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        overlayAlpha.animateTo(1f, tween(250))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .graphicsLayer {
                alpha = overlayAlpha.value
            }
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun RepoLinkButton(icon: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.bounceClick().clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(PureWhite.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                null,
                tint = YellowAccent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = PureWhite.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
fun SkillNodeTrigger(text: String, backgroundColor: Color, textColor: Color, onClick: () -> Unit) {
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
fun DashboardActionCard(
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
        Spacer(Modifier.width(10.dp))
        Text(text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ConsoleRevealEffect(text: String, fontSize: Int, color: Color, fontWeight: FontWeight) {
    var display by remember { mutableStateOf("") }
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789*&^%"

    LaunchedEffect(text) {
        display = text.map { chars.random() }.joinToString("")
        for (i in text.indices) {
            delay(100)
            display = display.take(i) + text[i] +
                    display.substring(i + 1).map { chars.random() }.joinToString("")
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

@Composable
fun PhysicsEngineOverlay(languages: List<Language>, onDismiss: () -> Unit) {
    val overlayAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        overlayAlpha.animateTo(1f, tween(250))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .graphicsLayer {
                alpha = overlayAlpha.value
            }
    ) {
        val width = maxWidth.value
        val height = maxHeight.value
        val minRadius = width * 0.11f
        val maxRadius = width * 0.16f

        val balls = remember {
            languages.mapIndexed { i, lang ->
                Ball(
                    lang,
                    width / 2 + (Random.nextFloat() - 0.5f) * (width * 0.4f),
                    -height * 0.3f - i * (maxRadius * 2.2f),
                    (Random.nextFloat() - 0.5f) * 40f,
                    0f,
                    minRadius + Random.nextFloat() * (maxRadius - minRadius)
                )
            }.toMutableList()
        }

        var trigger by remember { mutableLongStateOf(0L) }

        LaunchedEffect(Unit) {
            val gravity = 1200f
            val drag = 0.98f
            val dt = 0.016f

            while (isActive) {
                balls.forEach { ball ->
                    ball.vy += gravity * dt
                    ball.vx *= drag
                    ball.vy *= drag
                    ball.x += ball.vx * dt
                    ball.y += ball.vy * dt
                }

                repeat(3) {
                    balls.forEach { ball ->
                        // Bottom boundary
                        if (ball.y + ball.radius > height) {
                            ball.y = height - ball.radius
                            ball.vy *= -0.6f
                        }

                        // Left boundary
                        if (ball.x - ball.radius < 0) {
                            ball.x = ball.radius
                            ball.vx *= -0.6f
                        }

                        // Right boundary
                        if (ball.x + ball.radius > width) {
                            ball.x = width - ball.radius
                            ball.vx *= -0.6f
                        }
                    }

                    // Ball collision
                    for (i in balls.indices) {
                        for (j in i + 1 until balls.size) {
                            val b1 = balls[i]
                            val b2 = balls[j]
                            val dx = b2.x - b1.x
                            val dy = b2.y - b1.y
                            val distSq = dx * dx + dy * dy
                            val minDist = b1.radius + b2.radius

                            if (distSq < minDist * minDist) {
                                val dist = sqrt(distSq).coerceAtLeast(0.1f)
                                val nx = dx / dist
                                val ny = dy / dist
                                val overlap = minDist - dist

                                b1.x -= nx * overlap * 0.5f
                                b2.x += nx * overlap * 0.5f
                                b1.y -= ny * overlap * 0.5f
                                b2.y += ny * overlap * 0.5f

                                val relVx = b2.vx - b1.vx
                                val relVy = b2.vy - b1.vy
                                val velAlongNormal = relVx * nx + relVy * ny

                                if (velAlongNormal < 0) {
                                    val impulse = velAlongNormal * 0.8f
                                    b1.vx += nx * impulse
                                    b1.vy += ny * impulse
                                    b2.vx -= nx * impulse
                                    b2.vy -= ny * impulse
                                }
                            }
                        }
                    }
                }

                trigger++
                delay(16)
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(PureWhite.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Rounded.Close, null, tint = PureWhite)
            }

            balls.forEach { ball ->
                key(ball.language.name, trigger) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = (ball.x - ball.radius).dp.toPx()
                                translationY = (ball.y - ball.radius).dp.toPx()
                            }
                            .size((ball.radius * 2).dp)
                            .shadow(8.dp, CircleShape)
                            .background(ball.language.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ball.language.name,
                            fontSize = (ball.radius * 0.32f).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.bounceClick() = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "bounce"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
        .pointerInput(Unit) {
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
    context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:aryanb3244@gmail.com".toUri()))
}

fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}