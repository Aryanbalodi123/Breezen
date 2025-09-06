package com.example.askquestion.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.askquestion.R
import com.example.askquestion.theme.AppColors
import com.example.askquestion.theme.CustomTypography
import com.example.askquestion.theme.FunnelDisplayFamily
import com.example.askquestion.theme.gradientBackground


@Stable
@Composable
fun AppBackground(): Brush {
    return remember {
        Brush.verticalGradient(
            colors = listOf(
                AppColors.DarkBackground,
                Color(0xFF0F0F0F),
                Color(0xFF1A1A1C),
                AppColors.SurfaceBackground
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeContent(navController: NavController) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
                .verticalScroll(rememberScrollState())



        ) {

            Row(Modifier
                .fillMaxWidth()
                .height(50.dp)) {
                Text(
                    "Breezen",
                    color = Color.White,
                    style = CustomTypography.titleLarge.copy(
                        fontFamily = FunnelDisplayFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(Modifier.weight(1f))
                Button(
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ), onClick = { null }) {
                    Icon(
                        painter = painterResource(R.drawable.heart),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))


         HeaderSection()

            Spacer(modifier = Modifier.height(28.dp))
//            Text("“If you want something done right, do it yourself.”", style = CustomTypography.displayMedium.copy(fontFamily = InstrumentalSerifFamily, fontSize = 36.sp), modifier=Modifier.padding(horizontal = 16.dp), color = Color.White)
            Spacer(modifier = Modifier.height(28.dp))


            FeaturedSection(navController)
            Spacer(modifier = Modifier.height(28.dp))



            ChatBot()
            Spacer(modifier = Modifier.height(28.dp))


        }
    }
}


@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape = RoundedCornerShape(bottomEnd = 120.dp))
    ) {
        Row {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .gradientBackground(
                            listOf(
                                Color.Black,
                                Color.Black,
                                Color.Black,

                                Color(0xFF294577),
                                Color(0xFF91658f),
                                Color(0xFFc8b2c7)

                            ), angle = 45f
                        )
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "GOOD MORNING ARYAN",
                style = CustomTypography.bodySmall.copy(
                    letterSpacing = 2.sp,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Text(
                "Await Rain",
                style = CustomTypography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    fontSize = 48.sp
                ),
                color = Color.White
            )
            Text(
                "FOREST AMBIENCE",
                style = CustomTypography.bodySmall.copy(

                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 12.sp
                ),
                color = Color.White
            )

            Text(
                "15 MIN",
                style = CustomTypography.bodySmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.height(24.dp))
            IconButton(
                onClick = { /* handle send */ },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                      Color.White
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    modifier = Modifier.size(22.dp),
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }


    }


}






@Composable
fun FeaturedSection(navController: NavController) {


    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FeatureSectionCard1()
        }
        item {
            FeatureSectionCard2()
        }


    }

}



@Composable
fun FeatureSectionCard1() {
    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .clip(shape = RoundedCornerShape(16.dp))

            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF012f46), Color(0xFF00090e), Color.Black),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()


        ) {
            Image(
                painter = painterResource(R.drawable.gradient_circles),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier

                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "10:00 min",
                    color = Color.White,
                    style = CustomTypography.titleSmall,

                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Quiet Flight",
                    color = Color.White,
                    style = CustomTypography.bodyLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Thin
                    ),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Take a journey through quiet sanctuary and blissful resonance",
                    color = Color.White.copy(alpha = 0.8f),
                    style = CustomTypography.bodySmall.copy(fontSize = 14.sp),

                    )
                Spacer(Modifier.height(10.dp))
                IconButton(
                    onClick = { /* handle send */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF012f46), Color(0xFF07a796))
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureSectionCard2() {
    Column(
        modifier = Modifier
            .height(350.dp)
            .width(250.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFdde46f),
                        Color(0xFF68a095),
                        Color(0xFF21366d),
                        Color(0xFF111333)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Images positioned absolutely to break out of padding constraints
            Row(
                horizontalArrangement = Arrangement.spacedBy((-50).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.End)
                    .offset(x = 16.dp) // Move beyond the right padding
            ) {
                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(rotationY = 45f)
                )

                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer(rotationY = 45f)
                )

                Image(
                    painter = painterResource(R.drawable.yellow_blue_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(190.dp)
                        .graphicsLayer(rotationY = 45f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "10:00 min",
                    color = Color.White,
                    style = CustomTypography.titleSmall,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Quiet Flight",
                    color = Color.White,
                    style = CustomTypography.bodyLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Thin
                    ),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Take a journey through quiet sanctuary and blissful resonance",
                    color = Color.White.copy(alpha = 0.8f),

                    style = CustomTypography.bodySmall.copy(fontSize = 14.sp),
                )
                Spacer(Modifier.height(10.dp))
                IconButton(
                    onClick = { /* handle send */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(           Color(0xFFdde46f),
                                    Color(0xFF68a095),)
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChatBot() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black) // true black base
    ) {
        // Emitting effect behind mandala
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3A9F8F).copy(alpha = 0.4f), // teal glow
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        // 🌿 Welcome meditation text
        Text(
            text = "How are you feeling today?",
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )

        // Mandala in the center
        Image(
            painter = painterResource(R.drawable.chatbot_background),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(150.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f))
        )

        // Bottom textbox + send button (Glass style)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f)) // glass transparency
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
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
                cursorBrush = SolidColor(Color.White),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text("Type your thoughts...", color = Color.Gray)
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = { /* handle send */ },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
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
                    tint = Color.Black
                )
            }
        }
    }
}
