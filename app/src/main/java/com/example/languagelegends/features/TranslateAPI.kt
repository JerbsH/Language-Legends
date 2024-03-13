package com.example.languagelegends.features

import android.content.Context
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

interface TranslationCallback {
    fun onTranslationResult(result: String)
    fun onTranslationError(error: String)
}

class TranslateAPI(context: Context) {

    private val client = OkHttpClient()
    private val apiKey = getDeepLAPIKey(context)

    init {
        Log.d("DBG", "API Key: $apiKey")
    }

    fun translate(
        text: String?,
        targetLanguage: String,
        callback: TranslationCallback
    ){
        Log.d("DBG", "Translating text: $text to language: $targetLanguage")

        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body: RequestBody = "text=$text&target_lang=$targetLanguage".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api-free.deepl.com/v2/translate")
            .post(body)
            .addHeader("Authorization", "DeepL-Auth-Key $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DBG", "Translation request failed", e)
                callback.onTranslationError("Translation request failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("DBG", "Unexpected code ${response.code}")
                    Log.e("DBG", "Response message: ${response.message}")
                    Log.e("DBG", "Response body: ${response.body?.string()}")
                    callback.onTranslationError("Unexpected code ${response.code}")
                    return
                }

                val result = response.body?.string() ?: ""
                val jsonObject = JSONObject(result)
                val translationsArray = jsonObject.getJSONArray("translations")
                val firstTranslationObject = translationsArray.getJSONObject(0)
                val translatedText = firstTranslationObject.getString("text")

                Log.d("DBG", "Translation result: $translatedText")
                callback.onTranslationResult(translatedText)
            }
        })
    }

    private fun getDeepLAPIKey(context: Context): String? {
        val jsonString: String
        try {
            val inputStream = context.assets.open("deeplAPIKey.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("DBG", "Error reading API key from assets", e)
            return null
        }

        val jsonObject = JSONObject(jsonString)
        val apiKey = jsonObject.getString("deepl_API_KEY")
        Log.d("DBG", "Retrieved API key: $apiKey")
        return apiKey
    }
}
