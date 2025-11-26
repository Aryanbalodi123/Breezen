package com.example.breezen.feature.breathe.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class BreathingTechnique(
    val id: Int,
    val name: String,
    val emoji: String,
    val shortDescription: String,
    val fullDescription: String,
    val inhaleTime: Int,
    val holdTime: Int,
    val exhaleTime: Int,
    val pauseTime: Int,
    val instructions: List<String>,
    val benefits: String
)

data class RingSpec(val size: Dp, val color: Color)