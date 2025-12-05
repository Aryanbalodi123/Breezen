package com.example.breezen.feature.home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.R
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.BrandGreen
import com.example.breezen.core.ui.theme.BrandGreenDarker
import com.example.breezen.core.ui.theme.CornerLarge
import com.example.breezen.core.ui.theme.DMSansFontFamily
import com.example.breezen.core.ui.theme.FunnelDisplayFamily
import com.example.breezen.core.ui.theme.TextPrimary
import com.example.breezen.core.ui.theme.WhiteAlpha08
import com.example.breezen.core.ui.theme.WhiteAlpha12
import com.example.breezen.feature.chatbot.ChatViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatBotSection(
    chatViewModel: ChatViewModel,
    navController: NavController
) {

    var input by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(CornerLarge))       // Your corner shape
            .background(AppBlack)
    ) {

        Image(
            painter = painterResource(R.drawable.chatbot_component_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "How are you feeling today?",
            color = TextPrimary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,

            style = AppTypography.displayLarge,
            fontFamily = FunnelDisplayFamily,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 26.dp)
        )

        Image(
            painter = painterResource(R.drawable.chatbot_background),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(AppWhite.copy(alpha = 0.70f)),
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(CircleShape)
                .background(WhiteAlpha08)
                .border(1.dp, WhiteAlpha12, CircleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = AppWhite,
                    fontFamily = DMSansFontFamily
                ),
                cursorBrush = SolidColor(AppWhite),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (input.isNotBlank()) {
                            chatViewModel.sendMessage(input.trim())
                            input = ""
                            keyboard?.hide()
                            navController.navigate("chatbot")
                        }
                    }
                ),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (input.isEmpty()) {
                            Text(
                                "Message Zeni…",
                                color = AppWhite.copy(alpha = 0.45f),
                                fontFamily = DMSansFontFamily
                            )
                        }
                        inner()
                    }
                }
            )

            // ➤ Send Button
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        chatViewModel.sendMessage(input)
                        input = ""
                        keyboard?.hide()
                        navController.navigate("chatbot")
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(BrandGreenDarker, BrandGreen)
                        )
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = "Send",
                    tint = AppBlack,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}