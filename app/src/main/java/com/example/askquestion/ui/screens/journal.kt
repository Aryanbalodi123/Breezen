//package com.example.askquestion.ui.screens
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.safeContent
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.windowInsetsPadding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonColors
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FilterChip
//import androidx.compose.material3.Icon
//import androidx.compose.material3.LocalContentColor
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.focusModifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.askquestion.R
//import com.example.askquestion.theme.CustomTypography
//
//@Composable
//fun JournalScreen(navController: NavController){
//    val journalItems= listOf(
//        "Creativity Booster" to "Switch on your brain with a series of cognitive...",
//        "Creativity Booster" to "Switch on your brain with a series of cognitive...",
//        "Creativity Booster" to "Switch on your brain with a series of cognitive..."
//
//
//    )
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.topo_back),
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.matchParentSize()
//        )
//
//        Column{
//       Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(10.dp).fillMaxHeight(.25f)){
//           Column (modifier = Modifier.padding(top=30.dp)){
//               Button(onClick = { navController.popBackStack() }, modifier = Modifier.size(50.dp),    shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp),
//                   colors = ButtonDefaults.buttonColors(
//                   containerColor = Color.White,
//                   contentColor = Color.Black,
//
//               )) {
//                   Icon(painter = painterResource(R.drawable.left_icon) ,contentDescription = null, modifier = Modifier.size(30.dp)) }
//               Spacer(modifier = Modifier.weight(1f))
//               Text(text="JOURNAL", style = CustomTypography.titleSmall)
//               Text(text="Productivity.", style = CustomTypography.displayLarge)
//
//           }
//           Image(
//               painter = painterResource(id = R.drawable.journal),
//               contentDescription = null,
//               modifier = Modifier
//                   .width(300.dp)
//                   .padding(top = 30.dp)
//                   .align(Alignment.Bottom)
//                   .aspectRatio(1.5f),
//
//               contentScale = ContentScale.Fit
//           )
//       }
//
//
//            Column (verticalArrangement = Arrangement.spacedBy(10.dp)){
//                journalItems.forEach{ (heading,subheading) ->
//                    Box(modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp)
//                        .background(Color.White, shape = RoundedCornerShape(16.dp))
//                        .padding(top = 20.dp, start = 10.dp, end = 10.dp, bottom = 10.dp) ){
//                        Row {
//                            Column(modifier = Modifier.fillMaxWidth(.8f)) {
//                                Text(text=heading, style = CustomTypography.displayMedium)
//                                Text(text=subheading, style = CustomTypography.titleSmall, color = Color.Gray, softWrap = true)
//                            }
//                        }
//                    }
//
//                }
//            }
//
//        }
//    }
//}