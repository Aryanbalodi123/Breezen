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

    // -------------------------
    // Initialization & storage
    // -------------------------
    private val context = application.applicationContext // app context
    private val prefs: SharedPreferences =
        context.getSharedPreferences("breezen_cache", Context.MODE_PRIVATE) // small prefs
    private val historyFile = File(context.cacheDir, "chat_history.json") // persistent chat file

    // UI-visible messages list: Pair(role, text) where role = "USER" or "AI"
    var messages = mutableStateListOf<Pair<String, String>>()

    // Loading indicator exposed as StateFlow
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Session state machine
    enum class SessionState { IDLE, ASKING_QUESTIONS, COLLECTING_ANSWERS, FINAL_GENERATION }
    var state = SessionState.IDLE

    // Limits and counters
    var dailySessionCount = 0
    private val MAX_DAILY_SESSIONS = 5

    // Date tracking for daily reset (string format)
    @RequiresApi(Build.VERSION_CODES.O)
    private var lastSessionDate = LocalDate.now().toString()

    // Temporary memory for the current consultation
    private var currentProblemString = "" // user's initial problem
    private var followUpQuestions = mutableListOf<String>() // Q1..Qn from AI
    private var answers = mutableListOf<String>() // answers collected from user
    var currentQuestionIndex = 0 // index of next question to ask

    // Running background job for API tasks
    private var job: Job? = null

    init {
        // Load cached state immediately so UI restores previous session
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loadFromCache()
        }
    }

    // -------------------------
    // Main input handler
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(userMessage: String) {
        // ignore empty or while loading
        if (userMessage.isBlank() || _loading.value) return

        // reset daily counters if day changed
        checkSessionReset()

        when (state) {
            // Start a fresh consultation
            SessionState.IDLE -> {
                // enforce daily limit
                if (dailySessionCount >= MAX_DAILY_SESSIONS) {
                    addMessage("AI", "You have reached your daily limit of $MAX_DAILY_SESSIONS sessions. Let's chat tomorrow!")
                    return
                }

                addMessage("USER", userMessage) // show user's message
                currentProblemString = userMessage // remember main issue
                _loading.value = true

                // Phase 1: analyze and generate questions
                analyzeAndGenerateQuestions(userMessage)
            }

            // Collecting answers to previously asked follow-ups
            SessionState.COLLECTING_ANSWERS -> {
                addMessage("USER", userMessage) // append user's answer
                answers.add(userMessage) // store answer
                currentQuestionIndex++ // advance pointer
                saveToCache() // persist progress

                if (currentQuestionIndex < followUpQuestions.size) {
                    // ask next follow-up
                    askNextQuestion()
                } else {
                    // all questions answered -> produce final solution
                    _loading.value = true
                    state = SessionState.FINAL_GENERATION
                    generateFinalSolution()
                }
            }

            // other states: ignore new user messages while processing
            else -> {}
        }
    }

    // -------------------------
    // Phase 1: Analyze & produce questions (JSON-only)
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    private fun analyzeAndGenerateQuestions(problem: String) {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                // system instruction forces a strict JSON array output
                val systemInstruction = """
                    You are Breezen, an empathetic meditation expert.
                    User problem: "$problem"
                    
                    Task: Return ONLY a JSON Array of 3 short follow-up questions.
                    Constraint: NO intro text. strictly ["Q1", "Q2", "Q3"]
                """.trimIndent()

                // fresh request (no sliding window for this fresh consultation)
                val request = GeminiRequest(
                    contents = listOf(RequestContent(parts = listOf(RequestPart(text = systemInstruction))))
                )

                val responseText = callGeminiApi(request) // network call
                val questionsList = parseQuestionsJson(responseText) // parse JSON array

                if (questionsList.isEmpty()) {
                    // parsing failed or AI returned non-JSON -> fallback
                    handleError("I couldn't analyze that. Let's try to find a solution directly.")
                    generateFinalSolution()
                    return@launch
                }

                // initialize Q&A state
                followUpQuestions.clear()
                followUpQuestions.addAll(questionsList)
                answers.clear()
                currentQuestionIndex = 0

                // switch to collecting answers on the main thread and ask first question
                withContext(Dispatchers.Main) {
                    state = SessionState.COLLECTING_ANSWERS
                    askNextQuestion()
                }
            } catch (_: Exception) {
                // network or parsing exception
                handleError("Connection failed. Please try again.")
            } finally {
                // always hide loading on main thread
                withContext(Dispatchers.Main) { _loading.value = false }
            }
        }
    }

    // Ask the next follow-up question (shows it in UI)
    private fun askNextQuestion() {
        if (currentQuestionIndex < followUpQuestions.size) {
            val q = followUpQuestions[currentQuestionIndex]
            addMessage("AI", q)
        }
    }

    // -------------------------
    // Phase 2: Generate final solution (uses sliding window)
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateFinalSolution() {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Build consultation history text from problem + Q&A
                val consultationHistory = StringBuilder()
                consultationHistory.append("User's Main Issue: $currentProblemString\n")
                for (i in followUpQuestions.indices) {
                    consultationHistory.append("Breezen Asked: ${followUpQuestions[i]}\n")
                    if (i < answers.size) {
                        consultationHistory.append("User Answered: ${answers[i]}\n")
                    }
                }

                // Build smart request using sliding window helper
                val smartRequest = buildSmartContextRequest(
                    systemRole = "You are Breezen, a wise, warm meditation coach. Use 'You'. Validate their feelings first.",
                    userCurrentInput = "Based on this consultation history, give me a personalized solution:\n$consultationHistory"
                )

                // send to Gemini and get full text response
                val result = callGeminiApi(smartRequest)

                withContext(Dispatchers.Main) {
                    addMessage("AI", result) // show final solution
                    state = SessionState.IDLE // reset state
                    dailySessionCount++ // consume a session
                    saveToCache() // persist final state
                }
            } catch (e: Exception) {
                handleError("Failed to generate a solution.")
            } finally {
                withContext(Dispatchers.Main) { _loading.value = false }
            }
        }
    }

    // -------------------------
    // Sliding-window prompt builder (token saver)
    // -------------------------
    private fun buildSmartContextRequest(systemRole: String, userCurrentInput: String): GeminiRequest {
        val contents = mutableListOf<RequestContent>()

        // A. System anchor (always included)
        contents.add(RequestContent(parts = listOf(RequestPart(text = systemRole))))

        // B. Recent UI history (last 4 messages only)
        val recentHistory = messages.takeLast(4)
        recentHistory.forEach { pair ->
            // map role to "model" or "user" prefix for readability
            val role = if (pair.first == "AI") "model" else "user"
            contents.add(RequestContent(parts = listOf(RequestPart(text = "${role}: ${pair.second}"))))
        }

        // C. Current user input (the trigger)
        contents.add(RequestContent(parts = listOf(RequestPart(text = "user: $userCurrentInput"))))

        return GeminiRequest(contents = contents)
    }

    // -------------------------
    // Network helper
    // -------------------------
    private suspend fun callGeminiApi(request: GeminiRequest): String {
        // simple wrapper around GeminiService; returns first candidate text or empty
        val response = GeminiService.api.generateContent(request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    }

    // Parse a JSON array from raw text and return list of strings
    private fun parseQuestionsJson(rawText: String): List<String> {
        return try {
            // locate a JSON array inside the model's raw text
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

    // -------------------------
    // Local state helpers & persistence
    // -------------------------
    private fun addMessage(role: String, text: String) {
        messages.add(role to text) // append to UI list
        saveToCache() // persist after every change
    }

    private suspend fun handleError(msg: String) {
        // post error message to UI and reset state
        withContext(Dispatchers.Main) {
            addMessage("AI", msg)
            state = SessionState.IDLE
        }
    }

    // Persist small bits to SharedPreferences and full history to file
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveToCache() {
        viewModelScope.launch(Dispatchers.IO) {
            // save counters & date
            prefs.edit().apply {
                putInt("daily_count", dailySessionCount)
                putString("last_date", lastSessionDate)
            }.apply()

            // save message history as JSON array
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

    // Load counters and history from disk (called at init)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadFromCache() {
        // restore counters
        dailySessionCount = prefs.getInt("daily_count", 0)
        lastSessionDate = prefs.getString("last_date", LocalDate.now().toString()) ?: LocalDate.now().toString()
        checkSessionReset() // ensure date consistency

        // restore message list
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

    // Reset daily counters when date changes
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkSessionReset() {
        val today = LocalDate.now().toString()
        if (today != lastSessionDate) {
            lastSessionDate = today
            dailySessionCount = 0
            saveToCache()
        }
    }

    // Clear current chat state (UI + memory)
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
