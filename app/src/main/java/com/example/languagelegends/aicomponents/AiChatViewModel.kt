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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume


// Here are all the functionalities needed for Hexascibe VertexAI-KT to work (https://github.com/hexascribe/vertexai-kt)
class AiChatViewModel(private val application: Application) : ViewModel() {
    var menuVisibility = MutableLiveData<Boolean>()
    var topic = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    private var projectId: String? = null
    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0

    //Initialize the VertexAI
    //TODO: Replace the accessToken and projectID with values from a file or a secure location
    private var vertexAI: VertexAI? = null

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            val currentAccessToken = sharedPreferences.getString("accessToken", null)
            val currentProjectId = sharedPreferences.getString("projectId", null)
            val tokenExpirationTime = sharedPreferences.getLong("tokenExpirationTime", 0)
            Log.d("DBG", "MyPrefs: $currentProjectId")

            var currentTime = System.currentTimeMillis()
            if (currentAccessToken != null && currentProjectId != null && currentTime < tokenExpirationTime) {
                accessToken = currentAccessToken
                projectId = currentProjectId
                this@AiChatViewModel.tokenExpirationTime = tokenExpirationTime
                vertexAI = VertexAI.Builder()
                    .setAccessToken(currentAccessToken)
                    .setProjectId(currentProjectId)
                    .build()
            } else {
                val (newAccessToken, newProjectId) = generateAccessToken()
                accessToken = newAccessToken
                projectId = newProjectId
                currentTime = System.currentTimeMillis() // update currentTime
                this@AiChatViewModel.tokenExpirationTime =
                    currentTime + 60 * 60 * 1000 // +1 hour to the current time
                sharedPreferences.edit().apply {
                    putString("accessToken", newAccessToken)
                    putString("projectId", newProjectId)
                    putLong("tokenExpirationTime", this@AiChatViewModel.tokenExpirationTime)
                    apply()
                }
                vertexAI = VertexAI.Builder()
                    .setAccessToken(newAccessToken)
                    .setProjectId(newProjectId)
                    .build()
            }

            // Calculate the remaining time for the token to expire
            currentTime = System.currentTimeMillis()
            val remainingTimeMillis = tokenExpirationTime - currentTime
            val remainingTimeSeconds = remainingTimeMillis / 1000
            Log.d("DBG", "Remaining time for token to expire: $remainingTimeSeconds seconds")
        }
    }

    private suspend fun generateAccessToken(): Pair<String, String> = withContext(Dispatchers.IO) {
        val serviceAccountKeyPath = "keyfile.json"
        val targetScopes = listOf("https://www.googleapis.com/auth/cloud-platform")

        val inputStream = application.assets.open(serviceAccountKeyPath)
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

        // Update the VertexAI instance with the new access token
        vertexAI = VertexAI.Builder()
            .setAccessToken(tokenValue)
            .setProjectId(projectId)
            .build()

        return@withContext Pair(tokenValue, projectId)
    }

    //Initialize the textRequest for VertexAI
    //TODO: May need to change model for chatting
    private val textRequest by lazy {
        vertexAI?.textRequest()
            ?.setModel("text-unicorn")
            ?.setTemperature(0.8)
            ?.setMaxTokens(256)
            ?.setTopK(40)
            ?.setTopP(0.8) ?: throw IllegalStateException("VertexAI not initialized")
    }

    //Get a question from the AI based on the chosen topic
    private suspend fun executeRequest(text: String): String {
        val resultFlow = MutableStateFlow<String?>(null)

        viewModelScope.launch {
            val result = textRequest.execute(text).getOrThrow()
            resultFlow.value = result
            Log.d("DBG", "Result: $result")
        }

        return suspendCancellableCoroutine { continuation ->
            val collector = viewModelScope.launch {
                resultFlow.collect { result ->
                    if (result != null) {
                        continuation.resume(result)
                    }
                }
            }

            continuation.invokeOnCancellation {
                // Cancel the collector if coroutine is cancelled
                collector.cancel()
            }
        }
    }

    fun onAskMeAQuestion() {
        //TODO: Implement the logic to ask a different question everytime and check the response if it is correct
        viewModelScope.launch {
            response.value =
                executeRequest("Ask me to translate phrase with the theme of ${topic.value}")
            //Log.d("DBG", "Response: ${response.value}")
        }
    }
}