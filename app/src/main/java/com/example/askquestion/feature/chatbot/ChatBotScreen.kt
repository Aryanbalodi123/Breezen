package com.example.askquestion.feature.chatbot

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.askquestion.R
import com.example.askquestion.feature.chatbot.components.EmptyStateLarge
import com.example.askquestion.feature.chatbot.components.LoadingBubble
import com.example.askquestion.feature.chatbot.components.MessageBubble
import com.example.askquestion.feature.chatbot.components.TopHeader


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
        color = MaterialTheme.colorScheme.background // Use theme background
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
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary, // Use theme
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Generating response...",
                        color = MaterialTheme.colorScheme.onSurface, // Use theme
                        style = MaterialTheme.typography.bodySmall // Use theme
                    )
                }
            }
        }
    }
}