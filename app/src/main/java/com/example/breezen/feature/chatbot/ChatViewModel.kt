package com.example.breezen.feature.chatbot

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.network.GeminiRequest
import com.example.breezen.core.network.GeminiService
import com.example.breezen.core.network.RequestContent
import com.example.breezen.core.network.RequestPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs: SharedPreferences =
        context.getSharedPreferences("breezen_cache", Context.MODE_PRIVATE)
    private val historyFile = File(context.cacheDir, "chat_history.json")

    var messages = mutableStateListOf<Pair<String, String>>()
        private set

    var loading by mutableStateOf(false)
        private set

    enum class Emotion { SAD, ANXIOUS, ANGRY, LONELY, NEUTRAL, HOPEFUL }
    var currentEmotion by mutableStateOf(Emotion.NEUTRAL)
        private set

    private var longTermInsight = ""

    var dailySessionCount by mutableIntStateOf(0)
        private set

    private val MAX_DAILY_SESSIONS = 15
    private var lastSessionDate = LocalDate.now().toString()

    private var job: Job? = null
    private var lastMessageRole: String? = null

    private val greetingReplies = listOf(
        "Hey. I’m here—what’s on your mind?",
        "Hi,What’s going on?",
        "I’m listening.",
        "Hey. Want to talk?"
    )

    private val acknowledgementReplies = listOf(
        "Got it.",
        "Okay.",
        "Makes sense.",
        "I hear you."
    )

    private val fallbackReplies = listOf(
        "I’m here. Want to try saying that another way?",
        "Let’s slow this down—tell me a bit more.",
        "I didn’t fully catch that, but I’m listening.",
        "Go on. What’s been weighing on you?"
    )

    init {
        loadData()

    }

    fun sendMessage(input: String) {
        if (input.isBlank()) return

        if (isGreeting(input)) {
            addMessage("USER", input)
            addMessage("AI", greetingReplies.random())
            return
        }

        if (isAcknowledgement(input)) {
            addMessage("USER", input)
            addMessage("AI", acknowledgementReplies.random())
            return
        }

        if (checkDailyLimitReached()) {
            addMessage("AI", "We've talked a lot today. Let's continue tomorrow.")
            return
        }

        addMessage("USER", input)

        if (isCrisis(input)) {
            addMessage(
                "AI",
                "If you're feeling unsafe, please reach out to a local helpline or someone you trust right now."
            )
            return
        }

        loading = true
        job?.cancel()
        analyzeInput(input)
    }

    fun clearChat() {
        messages.clear()
        longTermInsight = ""
        currentEmotion = Emotion.NEUTRAL
        if (historyFile.exists()) historyFile.delete()
        saveData()
        addMessage("AI", greetingReplies.random())
    }

    private fun analyzeInput(input: String) {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = callGeminiApi(buildSmartPrompt(input))
                withContext(Dispatchers.Main) {
                    loading = false
                    if (response.isBlank()) {
                        addMessage("AI", fallbackReplies.random())
                    } else {
                        processResponseSafely(response)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loading = false
                    addMessage("AI", fallbackReplies.random())
                }
            }
        }
    }

    private fun processResponseSafely(raw: String) {
        val parts = raw.split("|").map { it.trim() }
        if (parts.size < 4) {
            addMessage("AI", raw)
            incrementDailySession()
            return
        }

        val emotion = parts[1]
        val memory = parts[2]
        val reply = parts.subList(3, parts.size).joinToString(" | ")

        handleEmotion(emotion)
        updateMemory(memory)
        addMessage("AI", reply)
        incrementDailySession()
    }

    private fun handleEmotion(e: String) {
        currentEmotion = when {
            e.contains("SAD", true) -> Emotion.SAD
            e.contains("ANXIOUS", true) -> Emotion.ANXIOUS
            e.contains("ANGRY", true) -> Emotion.ANGRY
            e.contains("LONELY", true) -> Emotion.LONELY
            e.contains("HOPEFUL", true) -> Emotion.HOPEFUL
            else -> Emotion.NEUTRAL
        }
    }

    private fun updateMemory(fact: String) {
        if (fact.equals("NONE", true) || fact.length < 6) return
        if (!longTermInsight.contains(fact, true)) {
            longTermInsight = (longTermInsight + "; " + fact).takeLast(300)
            saveData()
        }
    }

    private fun isGreeting(text: String): Boolean {
        val t = text.trim().lowercase()
        return t.length <= 3 || t in setOf(
            "hi", "hey", "hii", "hello", "yo", "sup"
        )
    }

    private fun isAcknowledgement(text: String): Boolean {
        val t = text.trim().lowercase()
        return t in setOf(
            "ok", "okay", "cool", "nice",
            "fine", "alright", "yes", "no",
            "hmm", "hm", "hmmm"
        )
    }

    private fun isCrisis(text: String): Boolean {
        val t = text.lowercase()
        return t.contains("suicide") || t.contains("kill myself") || t.contains("want to die")
    }

    private fun checkDailyLimitReached(): Boolean {
        val today = LocalDate.now().toString()
        if (lastSessionDate != today) {
            dailySessionCount = 0
            lastSessionDate = today
            saveData()
            return false
        }
        return dailySessionCount >= MAX_DAILY_SESSIONS
    }

    private fun incrementDailySession() {
        dailySessionCount++
        saveData()
    }

    private fun addMessage(role: String, text: String) {
        if (role == "AI" && lastMessageRole == "AI") return
        messages.add(role to text)
        lastMessageRole = role
        viewModelScope.launch(Dispatchers.IO) { saveHistory() }
    }

    private suspend fun callGeminiApi(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(RequestContent(parts = listOf(RequestPart(text = prompt))))
        )
        val response = GeminiService.api.generateContent(request)
        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text ?: ""
    }

    private fun saveData() {
        prefs.edit()
            .putInt("daily_sessions", dailySessionCount)
            .putString("last_date", lastSessionDate)
            .putString("insight", longTermInsight)
            .apply()
    }

    private fun loadData() {
        dailySessionCount = prefs.getInt("daily_sessions", 0)
        lastSessionDate = prefs.getString("last_date", LocalDate.now().toString())!!
        longTermInsight = prefs.getString("insight", "") ?: ""
    }

    private fun saveHistory() {
        val arr = JSONArray()
        messages.takeLast(20).forEach {
            arr.put(JSONObject().apply {
                put("role", it.first)
                put("text", it.second)
            })
        }
        historyFile.writeText(arr.toString())
    }

    private fun buildSmartPrompt(input: String): String {
        return """
You are Zeni — a clear-thinking, honest friend. Not a therapist.

Context:
Memory: $longTermInsight

Message:
$input

Reply in ONE line only:
[INTENT]|[EMOTION]|[MEMORY]|[RESPONSE]

INTENT: EMOTIONAL, STUCK, CONFUSED, EXISTENTIAL, RELATIONAL, PRACTICAL, VENTING
EMOTION: SAD, ANXIOUS, ANGRY, LONELY, NEUTRAL, HOPEFUL
MEMORY: one useful long-term fact or NONE

RESPONSE (80–140 words, well-formatted):
- Use multiple short paragraphs
- Leave blank lines between ideas
- Explain the real issue
- Explain why it makes sense
- Give a realistic way forward
- Ask ONE thoughtful question
- No therapy language
- No generic motivation

Before finalizing, self-check clarity and usefulness.
""".trimIndent()
    }
}
