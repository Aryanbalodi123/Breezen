//package com.example.askquestion.ui.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.example.askquestion.theme.CustomTypography
//
//@Composable
//fun MeditateMainScreen(navController: NavController, items: List<Pair<String, String>>) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ) {
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(2),
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(items) { item ->
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    // ✅ Pill-shaped image
//                    AsyncImage(
//                        model = item.first,
//                        contentDescription = "Meditation Image",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .size(width = 150.dp, height = 80.dp)
//                            .clip(RoundedCornerShape(50.dp)) // Pill shape
//                    )
//
//                    // ✅ Text label below the image
//                    Text(
//                        text = item.second,
//                        style = CustomTypography.bodyLarge,
//                        color = Color.White,
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
//                }
//            }
//        }
//    }
//}
