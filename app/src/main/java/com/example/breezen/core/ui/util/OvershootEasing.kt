package com.example.breezen.core.ui.util

import androidx.compose.animation.core.Easing

fun overshootEasing(tension: Float = 2f): Easing {
    return Easing { fraction ->
        val t = fraction - 1.0f
        t * t * ((tension + 1.0f) * t + tension) + 1.0f
    }
}