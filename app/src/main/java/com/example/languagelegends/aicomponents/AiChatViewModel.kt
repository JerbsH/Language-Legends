package com.example.languagelegends.aicomponents

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hexascribe.vertexai.VertexAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream


/**
 * ViewModel class for managing AI chat functionalities using VertexAI-KT.
 * @property application Application instance to access application resources.
 */
// Here are all the functionalities needed for Hexascibe VertexAI-KT to work (https://github.com/hexascribe/vertexai-kt)
class AiChatViewModel(private val application: Application) : ViewModel() {

    // Constants
    companion object {
        const val ACCESS_TOKEN = "accessToken"
        const val PROJECT_ID = "projectId"
        const val TOKEN_EXPIRATION_TIME = "tokenExpirationTime"
        const val SERVICE_ACCOUNT_KEY_PATH = "keyfile.json"

    }

    // LiveData objects for managing UI state
    var menuVisibility = MutableLiveData<Boolean>()
    var topic = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    // Variables for managing VertexAI instance
    private var projectId: String? = null
    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0
    private var vertexAI: VertexAI? = null
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            initializeVertexAI()
        }
    }

    /**
     * Builds a VertexAI instance with the provided access token and project ID.
     * @param accessToken Access token for VertexAI.
     * @param projectId Project ID for VertexAI.
     */
    private fun buildVertexAIInstance(accessToken: String?, projectId: String?) {
        vertexAI = VertexAI.Builder()
            .setAccessToken(accessToken ?: throw IllegalStateException("Access token is null"))
            .setProjectId(projectId ?: throw IllegalStateException("Project ID is null"))
            .build()
    }

    /**
     * Initializes VertexAI instance by checking for existing access token and project ID,
     * or generating a new one if necessary.
     */
    private suspend fun initializeVertexAI() {
        try {
            val currentTime = System.currentTimeMillis()
            val (currentAccessToken, currentProjectId, tokenExpirationTime) = getSavedTokenInfo()

            if (currentAccessToken != null && currentProjectId != null && currentTime < tokenExpirationTime) {
                accessToken = currentAccessToken
                projectId = currentProjectId
                this@AiChatViewModel.tokenExpirationTime = tokenExpirationTime
            } else {
                val (newAccessToken, newProjectId) = generateAccessToken()
                saveTokenInfo(newAccessToken, newProjectId, currentTime)
            }

            buildVertexAIInstance(accessToken, projectId)
            remainingTokenTime()
        } catch (e: Exception) {
            Log.e("DBG", "Error initializing VertexAI: ${e.message}")
        }
    }

    /**
     * Retrieves saved token information from shared preferences.
     * @return Triple containing access token, project ID, and token expiration time.
     */
    private fun getSavedTokenInfo(): Triple<String?, String?, Long> {
        val currentAccessToken = sharedPreferences.getString(ACCESS_TOKEN, null)
        val currentProjectId = sharedPreferences.getString(PROJECT_ID, null)
        val tokenExpirationTime = sharedPreferences.getLong(TOKEN_EXPIRATION_TIME, 0)
        Log.d("DBG", "MyPrefs: $currentProjectId")
        return Triple(currentAccessToken, currentProjectId, tokenExpirationTime)
    }

    /**
     * Saves new token information to shared preferences.
     */
    private fun saveTokenInfo(
        newAccessToken: String,
        newProjectId: String,
        currentTime: Long
    ) {
        accessToken = newAccessToken
        projectId = newProjectId
        this@AiChatViewModel.tokenExpirationTime =
            currentTime + 60 * 60 * 1000 // +1 hour to the current time
        sharedPreferences.edit().apply {
            putString(ACCESS_TOKEN, newAccessToken)
            putString(PROJECT_ID, newProjectId)
            putLong(TOKEN_EXPIRATION_TIME, this@AiChatViewModel.tokenExpirationTime)
            apply()
        }
    }

    /**
     * Logs the remaining time for the access token to expire.
     */
    private fun remainingTokenTime() {
        val currentTime = System.currentTimeMillis()
        val remainingTimeMillis = tokenExpirationTime - currentTime
        val remainingTimeSeconds = remainingTimeMillis / 1000
        Log.d("DBG", "Remaining time for token to expire: $remainingTimeSeconds seconds")
    }

    /**
     * Generates a new access token and project ID.
     * @return Pair containing new access token and project ID.
     */
    private suspend fun generateAccessToken(): Pair<String, String> = withContext(Dispatchers.IO) {
        val targetScopes = listOf("https://www.googleapis.com/auth/cloud-platform")

        val inputStream = application.assets.open(SERVICE_ACCOUNT_KEY_PATH)
        val jsonContent = inputStream.bufferedReader()
            .use { it.readText() }

        val credentials =
            GoogleCredentials.fromStream(ByteArrayInputStream(jsonContent.toByteArray()))
                .createScoped(targetScopes)

        credentials.refreshIfExpired()
        val tokenValue = credentials.accessToken.tokenValue

        // Parse the JSON content to get the project_id
        val json = Gson().fromJson(jsonContent, JsonObject::class.java)
        val projectId = json.get("project_id").asString

        Log.d("DBG", "Generated access token: $tokenValue")

        buildVertexAIInstance(tokenValue, projectId)

        return@withContext Pair(tokenValue, projectId)
    }

    /**
     * Executes a request to VertexAI with the provided text.
     * @param text Text to be processed by VertexAI.
     * @return Result from VertexAI.
     */
    private suspend fun executeRequest(text: String): String {
        val resultFlow = MutableStateFlow<String?>(null)

        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            if (currentTime >= tokenExpirationTime) {
                initializeVertexAI()
            }

            val result = textRequest.execute(text).getOrThrow()
            resultFlow.value = result
            Log.d("DBG", "Result: $result")
        }

        return resultFlow.first { it != null } ?: ""
    }

    /**
     * Initializes the textRequest for VertexAI.
     */
    //TODO: May need to change model for chatting
    private val textRequest by lazy {
        vertexAI?.textRequest()
            ?.setModel("text-unicorn")
            ?.setTemperature(0.8)
            ?.setMaxTokens(256)
            ?.setTopK(40)
            ?.setTopP(0.8) ?: throw IllegalStateException("VertexAI not initialized")
    }

    /**
     * Triggers a question to be asked by VertexAI based on the current topic.
     */
    fun onAskMeAQuestion() {
        //TODO: Implement the logic to ask a different question everytime and check the response if it is correct
        viewModelScope.launch {
            response.value =
                executeRequest("Ask me to translate phrase with the theme of ${topic.value}")
        }
    }
}