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
import com.hexascribe.vertexai.features.TextRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

/** This is the ViewModel for the AI Chat feature of the application.
 * It handles the lifecycle of the AI chat, including initialization of the VertexAI instance,
 * checking token expiration, and executing text requests.
 * **/
class AiChatViewModel(private val application: Application) : ViewModel() {


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
    var response = MutableLiveData<String>()

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
        val remainingTimeSeconds = remainingTimeMillis / 1000
        Log.d(
            "DBG",
            "Remaining time for token to expire: $remainingTimeSeconds seconds"
        )
    }

    /** This function is called when the user asks a question.
     * It executes a text request to the VertexAI instance.
     **/
    fun onAskMeAQuestion() {
        viewModelScope.launch {
            Log.d("DBG", "Asking question")
            if (textRequest == null) {
                Log.e("DBG", "textRequest is not initialized")
                return@launch
            }

            val resultFlow = MutableStateFlow<String?>(null)

            withContext(Dispatchers.IO) {
                try {
                    val result =
                        textRequest?.execute("Ask me to translate phrase with the theme of ${topic.value}")
                            ?.getOrThrow()
                    resultFlow.value = result
                    Log.d("DBG", "Result: $result")
                } catch (e: Exception) {
                    Log.e("DBG", "Error executing request: ${e.message}")
                    throw e
                }
            }

            response.value = resultFlow.first { it != null } ?: ""
        }
    }
}