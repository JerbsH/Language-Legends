package com.example.languagelegends.features

/**
 * ai free chat message dataclass to store text,
 * check if text is from the user or from AI,
 * correctly display it in the chat screen
 */
data class Message(
    val text: String,
    val isFromUser: Boolean
)