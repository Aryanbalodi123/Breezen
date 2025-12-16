package com.example.breezen.feature.chatbot

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.breezen.R
import com.example.breezen.core.ui.theme.DarkGreen
import com.example.breezen.feature.chatbot.components.EmptyStateLarge
import com.example.breezen.feature.chatbot.components.LoadingBubble
import com.example.breezen.feature.chatbot.components.MessageBubble
import com.example.breezen.feature.chatbot.components.TopHeader
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun ChatBotScreen(
    navController: NavHostController,
    viewModel: ChatViewModel
) {


    // ✅ CORRECT:
    val loading by remember { derivedStateOf { viewModel.loading } }

    val messages = viewModel.messages
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val dailyCount = viewModel.dailySessionCount

    // Shared haze instance
    val hazeState = remember { HazeState() }

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = DarkGreen,
        topBar = {
            TopHeader(
                dailyCount = dailyCount,
                onNewChatClicked = { viewModel.clearChat() },
                navController = navController
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            // Background image
            Image(
                painter = painterResource(id = R.drawable.chatbot_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState)
            )

            // Foreground Chat UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
                ) {

                    // Empty State
                    if (messages.isEmpty() && !loading) {
                        item {
                            EmptyStateLarge { userPrompt ->
                                viewModel.sendMessage(userPrompt)
                            }
                        }
                    }

                    // Chat messages
                    itemsIndexed(messages) { index, (sender, message) ->

                        val isLast = index == messages.lastIndex

                        MessageBubble(
                            message = message,
                            sender = sender,
                            isLastMessage = isLast,
                            hazeState = hazeState,
                            onCopy = {
                                clipboard.setText(AnnotatedString(it))
                            },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, it)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            },
                            onRegenerate = {
                                val lastUserMsg = messages.lastOrNull { it.first == "USER" }?.second
                                if (lastUserMsg != null) viewModel.sendMessage(lastUserMsg)
                            }
                        )
                    }

                    // Loading bubble
                    if (loading) {
                        item {
                            LoadingBubble()
                        }
                    }
                }
            }
        }
    }
}
