package com.example.languagelegends.aicomponents

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hexascribe.vertexai.VertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Here are all the functionalities needed for Hexascibe VertexAI-KT to work (https://github.com/hexascribe/vertexai-kt)
class AiChatViewModel(private val application: Application) : ViewModel() {
    var menuVisibility = MutableLiveData<Boolean>()
    var topic = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    //Initialize the VertexAI
    //TODO: Replace the accessToken and projectID with values from a file or a secure location
    private val vertexAI by lazy {
        VertexAI.Builder()
            .setAccessToken("")
            .setProjectId("")
            .build()
    }
    //Initialize the textRequest for VertexAI
    //TODO: May need to change model for chatting
    private val textRequest by lazy {
        vertexAI.textRequest()
            .setModel("text-unicorn")
            .setTemperature(0.8)
            .setMaxTokens(256)
            .setTopK(40)
            .setTopP(0.8)
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
            Log.d("DBG", "Response: ${response.value}")
        }
    }
}