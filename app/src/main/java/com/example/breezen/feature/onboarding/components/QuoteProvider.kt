package com.example.breezen.feature.onboarding.components

import kotlin.random.Random

object QuoteProvider {
    private val quotes = listOf(
        "The mind is everything. What you think, you become.",
        "Peace comes from within. Do not seek it without.",
        "Quiet the mind, and the soul will speak.",
        "The present moment is filled with joy and happiness. If you are attentive, you will see it.",
        "Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure."
    )

    fun getRandomQuote(): String {
        return quotes[Random.nextInt(0, quotes.size)]
    }
}