package com.example.languagelegends.aicomponents

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelegends.R
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.features.LANGUAGES
import com.example.languagelegends.features.Message
import com.example.languagelegends.features.TranslateAPI
import com.example.languagelegends.features.TranslationCallback
import com.example.languagelegends.features.UserProfileViewModel
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hexascribe.vertexai.VertexAI
import com.hexascribe.vertexai.features.TextRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.Locale

/**
 * ViewModel for the AI Chat feature of the application.
 * Handles the lifecycle of the AI chat, including initialization of the VertexAI instance,
 * checking token expiration, and executing text requests.
 */
class AiChatViewModel(
    private val application: Application,
    userProfileViewModel: UserProfileViewModel
) : ViewModel() {

    var questionAskedLanguage = mutableStateOf(userProfileViewModel.selectedLanguage)

    // Constants used for SharedPreferences
    companion object {
        const val PROJECT_ID = "projectId"
        const val SERVICE_ACCOUNT_KEY_PATH = "keyfile.json"
        const val TOKEN_EXPIRATION_TIME = "tokenExpirationTime"
        const val ACCESS_TOKEN = "accessToken"
    }

    // LiveData objects to hold the state of the UI
    var menuVisibility = MutableLiveData<Boolean>()
    var topic = MutableLiveData<String>()
    var response = MutableLiveData<String?>()
    var isFreeChat = MutableLiveData<Boolean>()
    var chatVisible = MutableLiveData<Boolean>()

    private val userProfileDao: UserProfileDao =
        DatabaseProvider.getDatabase(application).userProfileDao()
    private val translateAPI = TranslateAPI(application)

    val languages = LANGUAGES
    var messages = MutableLiveData<List<Message>>()
    var isGeneratingQuestion = MutableLiveData<Boolean>()
    var isQuestionAsked = MutableLiveData<Boolean>()
    var userAnswer = mutableStateOf("")
    private var lastFewPrompts: MutableList<String> = mutableListOf()
    private var correctAnswer = MutableLiveData<String?>()
    var resultMessage = MutableLiveData<String?>()
    private val correctAnswerString = application.getString(R.string.correct_answer)
    private val incorrectAnswerTryAgainString =
        application.getString(R.string.incorrect_answer_try_again)

    // SharedPreferences to store the access token and project ID
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private var textRequest: TextRequest? = null

    init {
        viewModelScope.launch {
            initializeVertexAI()
            checkTokenExpirationPeriodically()
        }
    }

    /**
     * Initializes the VertexAI instance.
     * Checks if the current access token is valid, and if not, generates a new one.
     */
    private suspend fun initializeVertexAI() {
        try {
            val currentTime = System.currentTimeMillis()
            val currentProjectId = sharedPreferences.getString(PROJECT_ID, null)
            val currentAccessToken = sharedPreferences.getString(ACCESS_TOKEN, null)
            val tokenExpirationTime = sharedPreferences.getLong(TOKEN_EXPIRATION_TIME, 0)

            if (currentProjectId == null || currentAccessToken == null || currentTime >= tokenExpirationTime) {
                val (newAccessToken, newProjectId) = generateAccessToken()
                saveTokenInfo(newAccessToken, newProjectId, currentTime)
            }

            buildVertexAIInstance()
        } catch (e: Exception) {
            Log.e("DBG", "Error initializing VertexAI: ${e.message}")
        }
    }

    /**
     * Builds the VertexAI instance with the access token and project ID.
     */
    private fun buildVertexAIInstance() {
        val vertexAI = VertexAI.Builder()
            .setAccessToken(
                sharedPreferences.getString(ACCESS_TOKEN, null)
                    ?: throw IllegalStateException("Access token is null")
            )
            .setProjectId(
                sharedPreferences.getString(PROJECT_ID, null)
                    ?: throw IllegalStateException("Project ID is null")
            )
            .build()

        textRequest = vertexAI.textRequest()
            .setModel("text-unicorn")
            .setTemperature(0.8)
            .setMaxTokens(256)
            .setTopK(40)
            .setTopP(0.8)
    }

    /**
     * Saves the access token, project ID, and token expiration time to SharedPreferences.
     */
    private fun saveTokenInfo(newAccessToken: String, newProjectId: String, currentTime: Long) {
        val tokenExpirationTime = currentTime + 20 * 1000 // +1 hour to the current time
        sharedPreferences.edit().apply {
            putString(ACCESS_TOKEN, newAccessToken)
            putString(PROJECT_ID, newProjectId)
            putLong(TOKEN_EXPIRATION_TIME, tokenExpirationTime)
            apply()
        }
    }

    /**
     * Generates a new access token and project ID.
     */
    private suspend fun generateAccessToken(): Pair<String, String> = withContext(Dispatchers.IO) {
        val targetScopes = listOf("https://www.googleapis.com/auth/cloud-platform")

        val inputStream = application.assets.open(SERVICE_ACCOUNT_KEY_PATH)
        val jsonContent = inputStream.bufferedReader()
            .use { it.readText() }

        val credentials =
            GoogleCredentials.fromStream(ByteArrayInputStream(jsonContent.toByteArray()))
                .createScoped(targetScopes)

        credentials.refresh()

        val json = Gson().fromJson(jsonContent, JsonObject::class.java)
        val projectId = json.get("project_id").asString

        return@withContext Pair(credentials.accessToken.tokenValue, projectId)
    }

    /**
     * Checks the token expiration periodically.
     * If the token is expired, it generates a new one.
     */
    private suspend fun checkTokenExpirationPeriodically() {
        while (viewModelScope.isActive) {
            val currentTime = System.currentTimeMillis()
            val tokenExpirationTime = sharedPreferences.getLong(TOKEN_EXPIRATION_TIME, 0)
            if (currentTime >= tokenExpirationTime) {
                initializeVertexAI()
            } else {
                logRemainingTime(tokenExpirationTime, currentTime)
            }
            delay(5 * 1000) // Check every 5 seconds
        }
    }

    /**
     * Logs the remaining time for the token to expire.
     */
    private fun logRemainingTime(tokenExpirationTime: Long, currentTime: Long) {
        val remainingTimeMillis = tokenExpirationTime - currentTime
        remainingTimeMillis / 1000
    }

    /**
     * Gets the selected language country code.
     */
    private suspend fun getSelectedLanguageCountryCode(): String {
        val userProfile = userProfileDao.getAllUserProfiles().firstOrNull()
        return userProfile?.currentLanguage?.countryCode ?: "EN-GB"
    }

    /**
     * list of different prompt templates for AI to ask about selected topic
     */
    private fun getPrompts(): List<String> {
        val topicValue = topic.value ?: ""
        return listOf(
            "Give me a single word related to $topicValue",
            "Give me a six word sentence related to $topicValue",
            "I need a single word about $topicValue",
            "Please generate a six word sentence related to $topicValue",
            "Describe $topicValue in six words.",
            "What's your take on $topicValue in six words.",
            "Summarize $topicValue in six words.",
            "What does $topicValue mean to you in six words.",
            "Express your thoughts on $topicValue in six words.",
            "What's interesting about $topicValue in six words."
        )
    }

    /**
     * Requests the AI to generate a question based on the selected topic.
     */
    fun onAskMeAQuestion() {
        viewModelScope.launch {
            resetHint()
            isQuestionAsked.value = true
            isGeneratingQuestion.postValue(true)
            resultMessage.value = ""
            if (textRequest == null) {
                return@launch
            }
            try {
                withContext(Dispatchers.IO) {

                    val prompts = getPrompts()
                    var prompt = prompts.shuffled().first()
                    // Check if the prompt is in the list of last few prompts
                    while (prompt in lastFewPrompts) {
                        prompt = prompts.shuffled().first()
                    }
                    lastFewPrompts.add(prompt)

                    // If the list of last few prompts is too long, remove the oldest prompt
                    if (lastFewPrompts.size > 5) {
                        lastFewPrompts.removeAt(0)
                    }
                    //check token, if expired, generate new token and initialize VertexAI
                    val aiResponse: String? = try {
                        textRequest?.execute(prompt)?.getOrThrow()
                    } catch (e: Exception) {
                        if (e.message?.contains("UNAUTHENTICATED") == true) {
                            initializeVertexAI()
                            textRequest?.execute(prompt)?.getOrThrow()
                        } else {
                            throw e
                        }
                    }

                    val modifiedAiResponse = aiResponse?.substringAfter(": ")
                    val targetLanguageCode = getSelectedLanguageCountryCode()

                    translateAPI.translate(
                        modifiedAiResponse,
                        targetLanguageCode,
                        object : TranslationCallback {
                            override fun onTranslationResult(result: String) {
                                // Store the translated text as the correct answer
                                val modifiedResult = result.trimEnd('.').trim('"')
                                correctAnswer.postValue(modifiedResult)

                                translateAPI.translate(
                                    modifiedAiResponse,
                                    "EN-GB",
                                    object : TranslationCallback {
                                        override fun onTranslationResult(result: String) {
                                            response.postValue(result)
                                        }

                                        override fun onTranslationError(error: String) {
                                            Log.e("DBG", "Translation error: $error")
                                        }
                                    })
                            }

                            override fun onTranslationError(error: String) {
                                Log.e("DBG", "Translation error: $error")
                            }
                        })
                }
            } catch (e: Exception) {
                Log.e("DBG", "Error executing request: ${e.message}")
                throw e
            } finally {
                isGeneratingQuestion.postValue(false)
            }
        }
    }

    /**
     * Checks the correct translation answer of the user
     */
    fun checkAnswer() {
        viewModelScope.launch {
            val correctAnswer = correctAnswer.value?.replace("\\s".toRegex(), "")?.lowercase(
                Locale.ROOT
            )
            val userAnswerText = userAnswer.value.replace("\\s".toRegex(), "")
                .lowercase(Locale.ROOT)
            Log.d("DBG", "Correct answer: $correctAnswer")
            Log.d("DBG", "User answer: $userAnswerText")

            if (correctAnswer == userAnswerText) {
                resultMessage.value = correctAnswerString
                userAnswer.value = ""
                response.value = null
                resetHint()
            } else {
                resultMessage.value = incorrectAnswerTryAgainString
            }
        }
    }

    private var hintProgress = 0
    private val _hint = MutableStateFlow("")
    val hint: StateFlow<String> get() = _hint

    private fun updateHint(newHint: String) {
        _hint.value = newHint
    }

    /**
     * Request a hint from correct answer 1 word at a time
     */
    fun requestHint() {
        val currentCorrectAnswer = correctAnswer.value.orEmpty()

        if (currentCorrectAnswer.isNotBlank()) {
            // Split the correct answer into words
            val answerWords = currentCorrectAnswer.split(" ")
            val hintIndex = hintProgress % answerWords.size
            val hintWord = answerWords[hintIndex]

            hintProgress++

            // Check if hint progress exceeds the number of words
            if (hintProgress >= answerWords.size) {
                hintProgress = 0
            }
            updateHint(hintWord)
        }
    }

    private fun resetHint() {
        hintProgress = 0
        updateHint("")
    }

    /**
     * Handles the user's input in free chat mode.
     */
    fun onFreeChat(userInput: String) {
        if (userInput.isBlank()) {
            return
        }

        viewModelScope.launch {
            isQuestionAsked.value = true
            isGeneratingQuestion.postValue(true)
            resultMessage.value = ""

            // Update UI with the user's message
            val userMessage = Message(userInput, true)
            messages.postValue(messages.value?.plus(userMessage) ?: listOf(userMessage))

            try {
                withContext(Dispatchers.IO) {
                    // Process user input and get AI response
                    val aiResponse = textRequest?.execute(userInput)?.getOrThrow()
                    val modifiedAiResponse = aiResponse?.substringAfter(": ")

                    // Update UI with AI response
                    modifiedAiResponse?.let {
                        val currentMessages = messages.value ?: emptyList()

                        // Add AI message to the list
                        val updatedMessages = currentMessages + Message(it, false)
                        messages.postValue(updatedMessages)
                    }
                }
            } catch (e: Exception) {
                Log.e("DBG", "Error executing request: ${e.message}")
                throw e
            } finally {
                isGeneratingQuestion.postValue(false)
            }
        }
    }
}


