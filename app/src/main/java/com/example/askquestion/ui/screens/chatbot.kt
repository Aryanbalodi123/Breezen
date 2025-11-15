package com.example.askquestion.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.askquestion.R
import com.example.askquestion.network.GeminiRequest
import com.example.askquestion.network.GeminiService
import com.example.askquestion.network.RequestContent
import com.example.askquestion.network.RequestPart
import com.example.askquestion.theme.CustomTypography
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


class ChatViewModel : ViewModel() {
    var messages = mutableStateListOf<Pair<String, String>>() // Pair of <Sender, Message>
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var generationJob: Job? = null

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _loading.value) return

        messages.add("USER" to prompt)
        _loading.value = true

        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val lastUserPrompts = messages.filter { it.first == "USER" }.takeLast(5)
                    .joinToString("\n") { it.second }

                val request = GeminiRequest(
                    contents = listOf(
                        RequestContent(
                            parts = listOf(
                                RequestPart("Previous prompts: $lastUserPrompts\nCurrent Prompt: ${prompt.trim()} . Reply related to meditation and healthcare")
                            )
                        )
                    )
                )

                val response = withTimeout(30000) {
                    GeminiService.api.generateContent(request)
                }

                val fullText =
                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Sorry, I couldn't generate a response."

                withContext(Dispatchers.Main) {
                    messages.add("AI" to fullText)
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is CancellationException -> "Generation stopped."
                    is TimeoutCancellationException -> "Request timed out. Please try again."
                    else -> "Sorry, something went wrong."
                }
                Log.e("ChatBot", "Error: ", e)
                withContext(Dispatchers.Main) {
                    messages.add("AI" to errorMsg)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            }
        }
    }

    fun stopGenerating() {
        generationJob?.cancel()
        _loading.value = false
    }

    fun clearChat() {
        messages.clear()
    }
}

@Composable
fun ChatBotScreen(navController: NavHostController, viewModel: ChatViewModel) {
    val messages = viewModel.messages
    val loading by viewModel.loading.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // Black background
    ) {
        // Background vector drawable
        Image(
            painter = painterResource(R.drawable.chatbot_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopHeader(onNewChatClicked = { viewModel.clearChat() }, navController)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                if (messages.isEmpty() && !loading) {
                    item { EmptyStateLarge() }
                }

                itemsIndexed(messages) { _, (sender, message) ->
                    MessageBubble(
                        message = message,
                        sender = sender,
                        onCopy = { clipboardManager.setText(AnnotatedString(it)) },
                        onShare = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, it)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                        },
                        onRegenerate = {
                            messages.lastOrNull { m -> m.first == "USER" }?.second?.let {
                                viewModel.sendMessage(it)
                            }
                        }
                    )
                }

                if (loading) item { LoadingBubble() }
            }

            AnimatedVisibility(
                visible = loading,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFF69F0AE),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Generating response...",
                        color = Color(0xFFDDDDDD),
                        style = CustomTypography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TopHeader(onNewChatClicked: () -> Unit, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left button
        ActionButton(
            icon = Icons.Default.ArrowBack, contentDescription = "Back",
            onClick = {

                navController.popBackStack()
            },
        )

        // Center content
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF00C853), Color(0xFFB2FF59)),
                            start = Offset(0f, 0f),
                            end = Offset(100f, 100f)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "ZenAI",
                    color = Color.White,
                    style = CustomTypography.titleMedium
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painterResource(R.drawable.sparkles),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }

        // Right button
        ActionButton(
            icon = Icons.Default.Add,
            contentDescription = "New Chat",
            onClick = onNewChatClicked
        )
    }
}

@Composable
private fun EmptyStateLarge() {
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(24.dp))
            Text("Hi there!", style = CustomTypography.displayLarge, color = Color.White)
            Text("How can I help you?", style = CustomTypography.titleMedium, color = Color.White)
        }
    }
}

@Composable
private fun MessageBubble(
    message: String,
    sender: String,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        if (sender == "AI") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = Color(0xFF69F0AE),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Zeni AI", color = Color(0xFF69F0AE), style = CustomTypography.titleMedium)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF69F0AE).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = formatRichText(message),
                    style = CustomTypography.bodyMedium,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(8.dp))
            ActionButtonsRow(
                onCopy = { onCopy(message) },
                onShare = { onShare(message) },
                onRegenerate = onRegenerate
            )
        } else { // User message
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF00C853), Color(0xFFB2FF59)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(message, color = Color.Black, style = CustomTypography.bodyMedium)
                }
            }
        }
    }
}

private fun formatRichText(text: String): AnnotatedString {
    // A simple markdown parser for bold (**) and italics (*)
    return buildAnnotatedString {
        val pattern = Regex("(?<=\\s|^)(\\*\\*|\\*)([^*]+)(\\*\\*|\\*)(?=\\s|$)")
        var lastIndex = 0
        pattern.findAll(text).forEach { matchResult ->
            val (delimiter, content) = matchResult.destructured
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last

            if (startIndex > lastIndex) {
                append(text.substring(lastIndex, startIndex))
            }
            val style = if (delimiter == "**") SpanStyle(fontWeight = FontWeight.Bold)
            else SpanStyle(fontStyle = FontStyle.Italic)
            withStyle(style) {
                append(content)
            }
            lastIndex = endIndex + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
private fun ActionButtonsRow(onCopy: () -> Unit, onShare: () -> Unit, onRegenerate: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        ActionButton(icon = Icons.Default.ThumbUp, contentDescription = "Like", onClick = {})
        ActionButton(icon = Icons.Default.ThumbDown, contentDescription = "Dislike", onClick = {})
        ActionButton(
            icon = Icons.Default.ContentCopy, contentDescription = "Copy", onClick = onCopy
        )
        ActionButton(icon = Icons.Default.Share, contentDescription = "Share", onClick = onShare)
        ActionButton(
            icon = Icons.Default.Refresh, contentDescription = "Regenerate", onClick = onRegenerate
        )
    }
}

@Composable
private fun ActionButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(), label = "")

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(0xff181818))
            .border(1.dp, Color.White.copy(alpha = 0.04f), CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun LoadingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = ""
    )
    Row(
        modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            null,
            tint = Color(0xFF69F0AE),
            modifier = Modifier
                .size(18.dp)
                .rotate(rotation)
        )
        Spacer(Modifier.width(12.dp))
        CircularProgressIndicator(
            Modifier.size(18.dp), color = Color(0xFF69F0AE), strokeWidth = 2.dp
        )
        Spacer(Modifier.width(12.dp))
        Text("Thinking...", color = Color(0xFFDDDDDD), style = CustomTypography.bodyMedium)
    }
}

@Composable
fun BottomInputSection(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    loading: Boolean
) {
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = loading, enter = fadeIn() + slideInVertically(), exit = fadeOut()
            ) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Icon(
                        Icons.Default.Stop,
                        "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Stop generating", color = Color.White)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            MaterialTheme.shapes.extraLarge
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        if (input.isEmpty()) {
                            Text(
                                "Ask me anything...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    })
                val sendButtonEnabled = input.isNotBlank() && !loading
                IconButton(
                    onClick = onSend,
                    enabled = sendButtonEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (sendButtonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f
                        ),
                        contentColor = if (sendButtonEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f
                        )
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}