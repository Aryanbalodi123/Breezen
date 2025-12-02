package com.example.breezen.feature.chatbot

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.breezen.core.network.GeminiRequest
import com.example.breezen.core.network.GeminiService
import com.example.breezen.core.network.RequestContent
import com.example.breezen.core.network.RequestPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // ------------------------------------------------------
    // 1. INITIALIZATION & STORAGE
    // ------------------------------------------------------
    private val context = application.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("breezen_cache", Context.MODE_PRIVATE)
    private val historyFile = File(context.cacheDir, "chat_history.json")

    // UI States
    var messages = mutableStateListOf<Pair<String, String>>() // "USER" -> "msg", "AI" -> "msg"

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Session Logic
    enum class SessionState { IDLE, ASKING_QUESTIONS, COLLECTING_ANSWERS, FINAL_GENERATION }
    var state = SessionState.IDLE

    // Limits
    var dailySessionCount = 0
    private val MAX_DAILY_SESSIONS = 5
    @RequiresApi(Build.VERSION_CODES.O)
    private var lastSessionDate = LocalDate.now().toString()

    // Temporary Memory for the current session
    private var currentProblemString = ""
    private var followUpQuestions = mutableListOf<String>()
    private var answers = mutableListOf<String>()
    var currentQuestionIndex = 0

    private var job: Job? = null

    init {
        // Load data immediately when ViewModel is created
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loadFromCache()
        }
    }

    // ------------------------------------------------------
    // 2. MAIN INPUT HANDLER
    // ------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank() || _loading.value) return

        checkSessionReset()

        when (state) {
            // --- START NEW CONSULTATION ---
            SessionState.IDLE -> {
                if (dailySessionCount >= MAX_DAILY_SESSIONS) {
                    addMessage("AI", "You have reached your daily limit of $MAX_DAILY_SESSIONS sessions. Let's chat tomorrow!")
                    return
                }

                addMessage("USER", userMessage)
                currentProblemString = userMessage
                _loading.value = true

                // Trigger Phase 1: Analysis
                analyzeAndGenerateQuestions(userMessage)
            }

            // --- ANSWERING QUESTIONS ---
            SessionState.COLLECTING_ANSWERS -> {
                addMessage("USER", userMessage)
                answers.add(userMessage)
                currentQuestionIndex++
                saveToCache() // Save progress

                if (currentQuestionIndex < followUpQuestions.size) {
                    // Ask next pending question
                    askNextQuestion()
                } else {
                    // All answered -> Trigger Phase 2: Solution
                    _loading.value = true
                    state = SessionState.FINAL_GENERATION
                    generateFinalSolution()
                }
            }

            else -> {} // Ignore clicks during loading
        }
    }

    // ------------------------------------------------------
    // 3. PHASE 1: ANALYZE & GET QUESTIONS (JSON MODE)
    // ------------------------------------------------------
    private fun analyzeAndGenerateQuestions(problem: String) {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Specific System Instruction for JSON output
                val systemInstruction = """
                    You are Breezen, an empathetic meditation expert.
                    User problem: "$problem"
                    
                    Task: Return ONLY a JSON Array of 3 short follow-up questions.
                    Constraint: NO intro text. strictly ["Q1", "Q2", "Q3"]
                """.trimIndent()

                // We don't use sliding window here because it's a fresh start
                val request = GeminiRequest(
                    contents = listOf(RequestContent(parts = listOf(RequestPart(text = systemInstruction))))
                )

                val responseText = callGeminiApi(request)
                val questionsList = parseQuestionsJson(responseText)

                if (questionsList.isEmpty()) {
                    handleError("I couldn't analyze that. Let's try to find a solution directly.")
                    generateFinalSolution() // Fallback
                    return@launch
                }

                followUpQuestions.clear()
                followUpQuestions.addAll(questionsList)
                answers.clear()
                currentQuestionIndex = 0

                withContext(Dispatchers.Main) {
                    state = SessionState.COLLECTING_ANSWERS
                    askNextQuestion()
                }

            } catch (e: Exception) {
                handleError("Connection failed. Please try again.")
            } finally {
                withContext(Dispatchers.Main) { _loading.value = false }
            }
        }
    }

    private fun askNextQuestion() {
        if (currentQuestionIndex < followUpQuestions.size) {
            val q = followUpQuestions[currentQuestionIndex]
            addMessage("AI", q)
        }
    }

    // ------------------------------------------------------
    // 4. PHASE 2: FINAL SOLUTION (SLIDING WINDOW CONTEXT)
    // ------------------------------------------------------
    private fun generateFinalSolution() {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Manually build the "Consultation Context"
                // We combine the Problem + Q&A into a specific format for the AI
                val consultationHistory = StringBuilder()
                consultationHistory.append("User's Main Issue: $currentProblemString\n")

                for (i in followUpQuestions.indices) {
                    consultationHistory.append("Breezen Asked: ${followUpQuestions[i]}\n")
                    if (i < answers.size) {
                        consultationHistory.append("User Answered: ${answers[i]}\n")
                    }
                }

                // 2. Build the Smart Request (Sliding Window logic applied inside helper)
                val smartRequest = buildSmartContextRequest(
                    systemRole = "You are Breezen, a wise, warm meditation coach. Use 'You'. Validate their feelings first.",
                    userCurrentInput = "Based on this consultation history, give me a personalized solution:\n$consultationHistory"
                )

                // 3. Send
                val result = callGeminiApi(smartRequest)

                withContext(Dispatchers.Main) {
                    addMessage("AI", result)
                    state = SessionState.IDLE
                    dailySessionCount++
                    saveToCache()
                }

            } catch (e: Exception) {
                handleError("Failed to generate a solution.")
            } finally {
                withContext(Dispatchers.Main) { _loading.value = false }
            }
        }
    }

    // ------------------------------------------------------
    // 5. SLIDING WINDOW LOGIC (TOKEN SAVER)
    // ------------------------------------------------------
    /**
     * Creates a List<RequestContent> that includes:
     * 1. The System Prompt (Identity) - ALWAYS KEEP
     * 2. The Last N Messages (Context) - TRIMMED
     * 3. The New Prompt (Action)
     */
    private fun buildSmartContextRequest(systemRole: String, userCurrentInput: String): GeminiRequest {
        val contents = mutableListOf<RequestContent>()

        // A. SYSTEM PROMPT (The Anchor)
        // We instruct the AI on who it is.
        contents.add(RequestContent(parts = listOf(RequestPart(text = systemRole))))

        // B. SLIDING WINDOW HISTORY (The "Memory")
        // We only take the last 4 messages from the UI to show flow, ignoring the very old ones.
        // This saves tokens while keeping recent context.
        val recentHistory = messages.takeLast(4)

        recentHistory.forEach { pair ->
            // Map our "AI/USER" tags to Gemini's "model/user" roles
            val role = if (pair.first == "AI") "model" else "user"
            // We create a turn for the AI to read
            contents.add(RequestContent(parts = listOf(RequestPart(text = "${role}: ${pair.second}"))))
        }

        // C. CURRENT INPUT (The Trigger)
        contents.add(RequestContent(parts = listOf(RequestPart(text = "user: $userCurrentInput"))))

        return GeminiRequest(contents = contents)
    }

    // ------------------------------------------------------
    // 6. HELPER FUNCTIONS & NETWORK
    // ------------------------------------------------------

    private suspend fun callGeminiApi(request: GeminiRequest): String {
        val response = GeminiService.api.generateContent(request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    }

    private fun parseQuestionsJson(rawText: String): List<String> {
        return try {
            val startIndex = rawText.indexOf('[')
            val endIndex = rawText.lastIndexOf(']')
            if (startIndex == -1 || endIndex == -1) return emptyList()

            val jsonArray = JSONArray(rawText.substring(startIndex, endIndex + 1))
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) list.add(jsonArray.getString(i))
            list
        } catch (e: Exception) {
            Log.e("ChatViewModel", "JSON Parse Error: ${e.message}")
            emptyList()
        }
    }

    private fun addMessage(role: String, text: String) {
        messages.add(role to text)
        saveToCache()
    }

    private suspend fun handleError(msg: String) {
        withContext(Dispatchers.Main) {
            addMessage("AI", msg)
            state = SessionState.IDLE
        }
    }

    // ------------------------------------------------------
    // 7. PERSISTENCE (CACHE & PREFS)
    // ------------------------------------------------------

    private fun saveToCache() {
        viewModelScope.launch(Dispatchers.IO) {
            // Save Counters
            prefs.edit().apply {
                putInt("daily_count", dailySessionCount)
                putString("last_date", lastSessionDate)
            }.apply()

            // Save History to File
            try {
                val jsonArray = JSONArray()
                messages.forEach { pair ->
                    val obj = JSONObject()
                    obj.put("role", pair.first)
                    obj.put("text", pair.second)
                    jsonArray.put(obj)
                }
                historyFile.writeText(jsonArray.toString())
            } catch (e: Exception) {
                Log.e("Cache", "Save failed: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadFromCache() {
        // Load Counters
        dailySessionCount = prefs.getInt("daily_count", 0)
        lastSessionDate = prefs.getString("last_date", LocalDate.now().toString()) ?: LocalDate.now().toString()
        checkSessionReset()

        // Load History
        if (historyFile.exists()) {
            try {
                val text = historyFile.readText()
                val jsonArray = JSONArray(text)
                messages.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    messages.add(obj.getString("role") to obj.getString("text"))
                }
            } catch (e: Exception) {
                Log.e("Cache", "Load failed: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkSessionReset() {
        val today = LocalDate.now().toString()
        if (today != lastSessionDate) {
            lastSessionDate = today
            dailySessionCount = 0
            saveToCache()
        }
    }

    fun clearChat() {
        messages.clear()
        followUpQuestions.clear()
        answers.clear()
        currentQuestionIndex = 0
        currentProblemString = ""
        state = SessionState.IDLE
        saveToCache()
    }
}