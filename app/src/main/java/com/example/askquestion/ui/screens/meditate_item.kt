package com.example.askquestion.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


class MeditateViewModel: ViewModel() {
    val meditateItems by mutableStateOf <List<String>>(listOf("aryan", "singh" ,"arunima","tiwari"))


}
@Composable
fun MeditateItems(navController: NavController, viewModel: MeditateViewModel = viewModel()){
    val meditateItem = viewModel.meditateItems
    LazyColumn {

    }

    }

