package com.example.askquestion.feature.home.components

import android.annotation.SuppressLint
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.askquestion.R
import com.example.askquestion.core.ui.theme.DMSansFontFamily

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChatBotSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // This gradient is artistic and unique, so it stays
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3A9F8F).copy(alpha = 0.4f),
                            Color.Transparent
                        ), radius = 600f
                    )
                )
        )

        Text(
            text = "How are you feeling today?",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            fontFamily = DMSansFontFamily, // Apply creative font
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )

        Image(
            painter = painterResource(R.drawable.chatbot_background),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(150.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var inputText by remember { mutableStateOf("") }

            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text("Type your thoughts...", color = MaterialTheme.colorScheme.onSurface)
                    }
                    innerTextField()
                })

            IconButton(
                onClick = { /* handle send */ },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    // This gradient is artistic and unique, so it stays
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3A9F8F), Color(0xFF66E6C9))
                        )
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    modifier = Modifier.size(22.dp),
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}