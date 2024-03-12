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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.Locale

/** This is the ViewModel for the AI Chat feature of the application.
 * It handles the lifecycle of the AI chat, including initialization of the VertexAI instance,
 * checking token expiration, and executing text requests.
 * **/
class AiChatViewModel(private val application: Application, private val userProfileViewModel: UserProfileViewModel) : ViewModel() {



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
    var questionLanguage = MutableLiveData<String>()
    var isFreeChat = MutableLiveData<Boolean>()



    private val userProfileDao: UserProfileDao =
        DatabaseProvider.getDatabase(application).userProfileDao()
    private val translateAPI = TranslateAPI(application)

    //deepl languages
    val languages = LANGUAGES

    var messages = MutableLiveData<List<Message>>()
    var isGeneratingQuestion = MutableLiveData<Boolean>()
    var isQuestionAsked = MutableLiveData<Boolean>()
    var userAnswer = mutableStateOf("")
    private var correctAnswer = MutableLiveData<String?>()
    var resultMessage = MutableLiveData<String?>()
    private val correctAnswerString = application.getString(R.string.correct_answer)
    private val incorrectAnswerTryAgainString =
        application.getString(R.string.incorrect_answer_try_again)


    // SharedPreferences to store the access token and project ID
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private var textRequest: TextRequest? = null

    // Initialize the ViewModel
    init {
        viewModelScope.launch {
            initializeVertexAI()
            checkTokenExpirationPeriodically()
        }
    }

    /**
     *  This function initializes the VertexAI instance.
     *It checks if the current access token is valid, and if not, generates a new one.
     * **/
    private suspend fun initializeVertexAI() {
        try {
            val currentTime = System.currentTimeMillis()
            val currentProjectId = sharedPreferences.getString(PROJECT_ID, null)
            val currentAccessToken = sharedPreferences.getString(ACCESS_TOKEN, null)
            val tokenExpirationTime = sharedPreferences.getLong(TOKEN_EXPIRATION_TIME, 0)

            if (currentProjectId == null || currentAccessToken == null || currentTime >= tokenExpirationTime) {
                Log.d("DBG", "Generating new access token and project ID")
                val (newAccessToken, newProjectId) = generateAccessToken()
                saveTokenInfo(newAccessToken, newProjectId, currentTime)
            }

            buildVertexAIInstance()
        } catch (e: Exception) {
            Log.e("DBG", "Error initializing VertexAI: ${e.message}")
        }
    }


    /**
     * for testing purposes, need to change saveToken time also
     * run this function on top of the initializeVertexAi function
     * try/catch block
     * **/
    private fun invalidateCurrentToken() {
        sharedPreferences.edit().apply {
            remove(ACCESS_TOKEN)
            remove(PROJECT_ID)
            remove(TOKEN_EXPIRATION_TIME)
            apply()
        }
    }

    // This function builds the VertexAI instance with the access token and project ID.
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

    // This function saves the access token, project ID, and token expiration time to SharedPreferences.
    private fun saveTokenInfo(newAccessToken: String, newProjectId: String, currentTime: Long) {
        Log.d("DBG", "Saving token info")
        val tokenExpirationTime = currentTime + 60 * 60 * 1000 // +1 hour to the current time
        sharedPreferences.edit().apply {
            putString(ACCESS_TOKEN, newAccessToken)
            putString(PROJECT_ID, newProjectId)
            putLong(TOKEN_EXPIRATION_TIME, tokenExpirationTime)
            apply()
        }
        Log.d("DBG", "Token info saved")
        Log.d("DBG", "Access token: $newAccessToken")
    }

    // This function generates a new access token and project ID.
    private suspend fun generateAccessToken(): Pair<String, String> = withContext(Dispatchers.IO) {
        Log.d("DBG", "Generating access token")
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

        Log.d("DBG", "Generated project id: $projectId")

        return@withContext Pair(credentials.accessToken.tokenValue, projectId)
    }

    /** This function checks the token expiration periodically.
     * If the token is expired, it generates a new one.
     **/
    private suspend fun checkTokenExpirationPeriodically() {
        while (viewModelScope.isActive) {
            val currentTime = System.currentTimeMillis()
            val tokenExpirationTime = sharedPreferences.getLong(TOKEN_EXPIRATION_TIME, 0)
            if (currentTime >= tokenExpirationTime) {
                Log.d("DBG", "Token expired, generating new one")
                initializeVertexAI()
            } else {
                logRemainingTime(tokenExpirationTime, currentTime)
            }
            delay(5 * 1000) // Check every 5 seconds
        }
    }

    // This function logs the remaining time for the token to expire.
    private fun logRemainingTime(tokenExpirationTime: Long, currentTime: Long) {
        val remainingTimeMillis = tokenExpirationTime - currentTime
        remainingTimeMillis / 1000
    }

    private suspend fun getSelectedLanguageCountryCode(): String {
        val userProfile = userProfileDao.getAllUserProfiles().firstOrNull()
        return userProfile?.currentLanguage?.countryCode ?: "EN-GB" // default
    }

    /** This function is called when the user asks a question.
     * It executes a text request to the VertexAI instance.
     **/
    fun onAskMeAQuestion() {
        viewModelScope.launch {
            Log.d("DBG", "Asking question")
            questionLanguage.value = userProfileViewModel.selectedLanguageLiveData.value
            isQuestionAsked.value = true
            isGeneratingQuestion.postValue(true)
            resultMessage.value = ""
            if (textRequest == null) {
                Log.e("DBG", "textRequest is not initialized")
                return@launch
            }

            try {
                withContext(Dispatchers.IO) {
                    val randomNumber = (1..100).random()

                    // Send the API call to the AI
                    val aiResponse = textRequest?.execute(
                        "Give me a short phrase related to ${topic.value} $randomNumber"
                    )?.getOrThrow()

                    val modifiedAiResponse = aiResponse?.substringAfter(": ")

                    Log.d("DBG", "AI response: $modifiedAiResponse")

                    // Translate the AI response to the selected language
                    val targetLanguageCode = getSelectedLanguageCountryCode()

                    translateAPI.translate(
                        modifiedAiResponse,
                        targetLanguageCode,
                        object : TranslationCallback {
                            override fun onTranslationResult(result: String) {
                                // Store the translated text as the correct answer
                                val modifiedResult = result.trimEnd('.').trim('"')
                                correctAnswer.postValue(modifiedResult)
                                Log.d("DBG", "Set correctAnswer value: $modifiedResult")

                                // Translate the AI response to English and post it to the response LiveData
                                translateAPI.translate(
                                    modifiedAiResponse,
                                    "EN-GB",
                                    object : TranslationCallback {
                                        override fun onTranslationResult(result: String) {
                                            response.postValue(result)
                                            Log.d(
                                                "DBG",
                                                "Posted translated data to response: $result"
                                            )
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
                Log.d("DBG", correctAnswerString)
                userAnswer.value = ""
                response.value = null
            } else {
                resultMessage.value = incorrectAnswerTryAgainString
                Log.d("DBG", incorrectAnswerTryAgainString)
            }
        }
    }

    fun onFreeChat(userInput: String) {
        if (userInput.isBlank()) {
            Log.e("DBG", "User input is empty or contains only whitespace.")
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
                    Log.d("DBG", "AI response: $modifiedAiResponse")

                    // Update UI with AI response
                    modifiedAiResponse?.let {
                        val currentMessages = messages.value ?: emptyList()

                        // Add AI message to the list
                        val updatedMessages = currentMessages + Message(it, false)

                        // Post the updated list of messages
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

