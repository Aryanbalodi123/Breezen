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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // -------------------- Setup --------------------

    private val context = application.applicationContext
    private val prefs: SharedPreferences =
        context.getSharedPreferences("breezen_cache", Context.MODE_PRIVATE)
    private val historyFile = File(context.cacheDir, "chat_history.json")

    // -------------------- UI State --------------------

    var messages = mutableStateListOf<Pair<String, String>>()
        private set

    var loading by mutableStateOf(false)
        private set

    // -------------------- Intelligence --------------------

    enum class Emotion { SAD, ANXIOUS, ANGRY, LONELY, NEUTRAL, HOPEFUL }
    var currentEmotion by mutableStateOf(Emotion.NEUTRAL)
        private set

    private var userName: String? = null
    private var longTermInsight = ""

    // -------------------- Limits --------------------

    var dailySessionCount by mutableIntStateOf(0)
        private set

    private val MAX_DAILY_SESSIONS = 15
    private var lastSessionDate = LocalDate.now().toString()

    // -------------------- Internals --------------------

    private var job: Job? = null
    private var retryCount = 0
    private val MAX_RETRIES = 2
    private var lastMessageRole: String? = null

    init {
        loadData()
        if (messages.isEmpty()) {
            addMessage("AI", "I'm here. Tell me what's going on.")
        }
    }

    // ==================================================
    // Public API
    // ==================================================

    fun sendMessage(input: String) {
        if (input.isBlank()) return

        addMessage("USER", input)

        if (checkDailyLimitReached()) return

        if (isCrisis(input)) {
            addMessage(
                "AI",
                "If you're feeling unsafe or thinking about harming yourself, please reach out to a local helpline or a trusted person right now."
            )
            return
        }

        loading = true
        job?.cancel()
        retryCount = 0

        analyzeInput(input)
    }

    fun clearChat() {
        messages.clear()
        longTermInsight = ""
        currentEmotion = Emotion.NEUTRAL
        historyFile.delete()
        saveData()
    }

    // ==================================================
    // Core Logic
    // ==================================================

    private fun analyzeInput(input: String) {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = buildSmartPrompt(input)
                val response = callGeminiApi(prompt)

                withContext(Dispatchers.Main) {
                    processSmartResponse(response)
                    loading = false
                }
            } catch (e: Exception) {
                handleError(e, input)
            }
        }
    }

    // -------------------- PROMPT (Concise but Powerful) --------------------

    private fun buildSmartPrompt(input: String): String {

        val history = messages.takeLast(4)
            .joinToString("\n") { "${it.first}: ${it.second}" }

        return """
You are Zeni — a clear-thinking, honest friend. Not a therapist.

Context:
User: ${userName ?: "Friend"}
Memory: $longTermInsight
Recent:
$history

Message:
$input

Reply in ONE line only:
[INTENT]|[EMOTION]|[MEMORY]|[RESPONSE]

INTENT: EMOTIONAL, STUCK, CONFUSED, EXISTENTIAL, RELATIONAL, PRACTICAL, VENTING
EMOTION: SAD, ANXIOUS, ANGRY, LONELY, NEUTRAL, HOPEFUL
MEMORY: one useful long-term fact or NONE

RESPONSE (80–140 words, well-formatted):
- Use multiple short paragraphs (2–4 lines max each)
- Leave blank lines between paragraphs
- Use bullet points or numbered steps if helpful
- Clearly explain the real issue
- Explain why it makes sense
- Give a realistic, step-by-step way forward
- Be direct, human, and grounded
- Ask ONE thoughtful question at the end
- No therapy language, no generic motivation
- No wall of text
- Use grounding only if anxiety is explicit


If self-harm risk → advise professional help.

Before finalizing the response, quickly self-check:
- Is the core problem clearly named?
- Did I explain why this problem happens?
- Did I give a concrete, realistic way forward?
- Does this reduce confusion or pressure?

If any answer is no, revise once internally.
Do not mention this check.

""".trimIndent()
    }

    private fun processSmartResponse(raw: String) {
        if (!raw.contains("|")) {
            addMessage("AI", raw)
            return
        }

        try {
            val parts = raw.split("|").map { it.trim() }

            val emotion = parts.getOrElse(1) { "NEUTRAL" }
            val memory = parts.getOrElse(2) { "NONE" }
            val reply = parts.getOrElse(3) { parts.last() }

            handleEmotion(emotion)
            updateMemory(memory)

            addMessage("AI", reply)
            incrementDailySession()

        } catch (e: Exception) {
            addMessage("AI", raw.replace("|", " "))
        }
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

    // ==================================================
    // Helpers
    // ==================================================

    private fun isCrisis(text: String): Boolean {
        val t = text.lowercase()
        return t.contains("suicide") || t.contains("kill myself") || t.contains("want to die")
    }

    private fun checkDailyLimitReached(): Boolean {
        if (LocalDate.now().toString() != lastSessionDate) {
            dailySessionCount = 0
            lastSessionDate = LocalDate.now().toString()
            saveData()
        }
        if (dailySessionCount >= MAX_DAILY_SESSIONS) {
            addMessage("AI", "We've talked a lot today. Let's continue tomorrow.")
            return true
        }
        return false
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

    // ==================================================
    // Persistence
    // ==================================================

    private fun saveData() {
        prefs.edit()
            .putInt("daily_sessions", dailySessionCount)
            .putString("last_date", lastSessionDate)
            .putString("user_name", userName)
            .putString("insight", longTermInsight)
            .apply()
    }

    private fun loadData() {
        dailySessionCount = prefs.getInt("daily_sessions", 0)
        lastSessionDate = prefs.getString("last_date", LocalDate.now().toString())!!
        userName = prefs.getString("user_name", null)
        longTermInsight = prefs.getString("insight", "") ?: ""

        if (historyFile.exists()) {
            try {
                val json = JSONArray(historyFile.readText())
                messages.clear()
                for (i in 0 until json.length()) {
                    val o = json.getJSONObject(i)
                    messages.add(o.getString("role") to o.getString("text"))
                }
            } catch (_: Exception) {
                historyFile.delete()
            }
        }
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

    private fun handleError(e: Exception, input: String) {
        if (retryCount < MAX_RETRIES) {
            retryCount++
            viewModelScope.launch {
                delay(1200L * retryCount)
                analyzeInput(input)
            }
        } else {
            loading = false
            addMessage("AI", "I'm having trouble responding right now. Try again in a moment.")
            retryCount = 0
        }
    }
}
