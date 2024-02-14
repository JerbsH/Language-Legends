package com.example.languagelegends.aicomponents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hexascribe.vertexai.VertexAI
import kotlinx.coroutines.launch

class AiChatViewModel : ViewModel() {
    private val vertexAI by lazy {
        VertexAI.Builder()
            .setAccessToken("ya29.a0AfB_byAhITPa0aH7VD9MRF2UtEg09u63bGQd2rSa3P3DM0GpsySQ_wp33unHoDUaIX01A2ULkK406vNXfeDcuI-x6OjRhbapkk_8U1vSdGxVbFpIvm7NI6kKRNBvdXzqhHnCjpN8UajcK8AYYs4TGkv0u4qSWnmpnsSqUXnTEMq8IPojw6eKqFLXcMN76wzq3wuASUxPp8Q3QPXH85RztoPGZdYYHsRTSMtTWHU-7rMMwB5cC1RHYwwslGiC7MvawrvZ-8MJGaLsDl4SIueWhvlpSesFi6d8ZuhlfI3TGTTeQ3B3RloJJlhg8Bhv4BriNlL2dVM8lXMiAuqDnQfiYLVo8EmlH2aIUTo3QvZom1ffjMcDOBY1TYZkA52XzCj3ouA880SvpNBZHDR8UjHp5udqfwGA6gaCgYKAaASARISFQHGX2MiTexUQAizewJsBK-KvzP6ZA0421")
            .setProjectId("onyx-elevator-414111")
            .build()
    }

    private val textRequest by lazy {
        vertexAI.textRequest()
            .setModel("text-bison")
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
