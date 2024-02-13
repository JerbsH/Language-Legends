package com.example.languagelegends.aicomponents

import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.io.IOException

class AiAuthCredentials {
    val keyFile: String = "NAME_OF_YOUR_KEY_FILE.json"
    private val credentials =
        try {
            FileInputStream(keyFile).use { stream ->
                GoogleCredentials.fromStream(stream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            }
        } catch (e: IOException) {
            e.printStackTrace()

        }
    //val accessToken: String
      //  get() = credentials.accessToken.tokenValue
}