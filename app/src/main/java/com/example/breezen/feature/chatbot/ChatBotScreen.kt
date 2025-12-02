package com.example.breezen.feature.chatbot

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.breezen.feature.chatbot.components.APP_BACKGROUND
import com.example.breezen.feature.chatbot.components.EmptyStateLarge
import com.example.breezen.feature.chatbot.components.LoadingBubble
import com.example.breezen.feature.chatbot.components.MessageBubble
import com.example.breezen.feature.chatbot.components.TopHeader

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatBotScreen(
    navController: NavHostController,
    viewModel: ChatViewModel
) {
    val messages = viewModel.messages
    val loading by viewModel.loading.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val count = viewModel.dailySessionCount

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = APP_BACKGROUND,
        topBar = {
            TopHeader(
                dailyCount = count,
                onNewChatClicked = { viewModel.clearChat() },
                navController = navController
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(APP_BACKGROUND)
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

                if (messages.isEmpty() && !loading) {
                    item {
                        EmptyStateLarge {
                            viewModel.sendMessage(it)
                        }
                    }
                }

                itemsIndexed(messages) { _, (sender, message) ->
                    MessageBubble(
                        message = message,
                        sender = sender,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.putExtra(Intent.EXTRA_TEXT, it)
                            intent.type = "text/plain"
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        },
                        onRegenerate = {
                            messages.lastOrNull { pair -> pair.first == "USER" }?.second?.let {
                                viewModel.sendMessage(it)
                            }
                        }
                    )
                }

                if (loading) item { LoadingBubble() }
            }
        }
    }
}
