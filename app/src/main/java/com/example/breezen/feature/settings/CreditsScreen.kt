package com.example.breezen.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breezen.core.ui.theme.AppBlack
import com.example.breezen.core.ui.theme.AppTypography
import com.example.breezen.core.ui.theme.AppWhite
import com.example.breezen.core.ui.theme.TextSecondary
import com.example.breezen.core.ui.theme.WhiteAlpha05
import com.example.breezen.core.ui.theme.WhiteAlpha10
import com.example.breezen.core.ui.theme.pastelColors

data class CreditItem(val title: String, val author: String, val type: String)

@Composable
fun CreditsScreen(navController: NavController) {
    val credits = listOf(
        CreditItem("Music library", "BreakingCopyright Music", "Music"),

    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBlack)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Credits",
            style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = AppWhite
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            itemsIndexed(items = credits) { idx, item ->
                ColorfulCreditCard(item = item, accent = pastelColors[idx % pastelColors.size])
            }
        }
    }
}

@Composable
fun ColorfulCreditCard(item: CreditItem, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WhiteAlpha05)
            .border(1.dp, WhiteAlpha10, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // Icon container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = AppBlack)
            }

            Spacer(Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppWhite
                )
                Text(
                    text = item.author,
                    style = AppTypography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Tag
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(6.dp)
            ) {
                Text(
                    text = item.type,
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
            }
        }
    }
}