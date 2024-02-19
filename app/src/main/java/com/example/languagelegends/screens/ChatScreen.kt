package com.example.languagelegends.screens

import android.util.Log
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.languagelegends.aicomponents.AiChatViewModel
import kotlinx.coroutines.coroutineScope

class ChatScreen {

    @Composable
    fun Chats() {
        val viewModel: AiChatViewModel = AiChatViewModel()
        val topic by viewModel.topic.observeAsState("")
        val menuVisibility by viewModel.menuVisibility.observeAsState(true)
        val response by viewModel.response.observeAsState("")

        Surface {
            if (menuVisibility) {
                viewModel.CardView()
            } else {
                viewModel.AiChat(topic, viewModel)
                Log.d("DBG", "Chatscreen response: $response")
            }

        }
    }

}
