package com.example.languagelegends.aicomponents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hexascribe.vertexai.VertexAI
import kotlinx.coroutines.launch

class AiChatViewModel : ViewModel() {
    private val vertexAI by lazy {
        VertexAI.Builder()
            .setAccessToken("")
            .setProjectId("")
            .build()
    }

    private val textRequest by lazy {
        vertexAI.textRequest()
            .setModel("gemini-pro")
            .setTemperature(0.8)
            .setMaxTokens(256)
            .setTopK(40)
            .setTopP(0.8)
    }

    fun executeRequest(text: String) {
        viewModelScope.launch {
            val result = textRequest.execute(text)
            println(result.getOrThrow())
        }
    }
}