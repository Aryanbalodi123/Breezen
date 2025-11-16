package com.example.askquestion.feature.chatbot

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askquestion.feature.chatbot.network.GeminiRequest
import com.example.askquestion.feature.chatbot.network.GeminiService
import com.example.askquestion.feature.chatbot.network.RequestContent
import com.example.askquestion.feature.chatbot.network.RequestPart
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


class ChatViewModel : ViewModel() {
    var messages = mutableStateListOf<Pair<String, String>>() // Pair of <Sender, Message>
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var generationJob: Job? = null

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _loading.value) return

        messages.add("USER" to prompt)
        _loading.value = true

        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val lastUserPrompts = messages.filter { it.first == "USER" }.takeLast(5)
                    .joinToString("\n") { it.second }

                val request = GeminiRequest(
                    contents = listOf(
                        RequestContent(
                            parts = listOf(
                                RequestPart("Previous prompts: $lastUserPrompts\nCurrent Prompt: ${prompt.trim()} . Reply related to meditation and healthcare")
                            )
                        )
                    )
                )

                val response = withTimeout(30000) {
                    GeminiService.api.generateContent(request)
                }

                val fullText =
                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Sorry, I couldn't generate a response."

                withContext(Dispatchers.Main) {
                    messages.add("AI" to fullText)
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is CancellationException -> "Generation stopped."
                    is TimeoutCancellationException -> "Request timed out. Please try again."
                    else -> "Sorry, something went wrong."
                }
                Log.e("ChatBot", "Error: ", e)
                withContext(Dispatchers.Main) {
                    messages.add("AI" to errorMsg)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            }
        }
    }

    fun stopGenerating() {
        generationJob?.cancel()
        _loading.value = false
    }

    fun clearChat() {
        messages.clear()
    }
}