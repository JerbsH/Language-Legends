package com.example.languagelegends.features

import android.content.Context
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

    fun translate(text: String, targetLanguage: String, callback: TranslationCallback) {
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body: RequestBody = "text=$text&target_lang=$targetLanguage".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.deepl.com/v2/translate")
            .post(body)
            .addHeader("Authorization", "Key $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onTranslationError("Translation request failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onTranslationError("Unexpected code ${response.code}")
                    return
                }

                val result = response.body?.string() ?: ""
                callback.onTranslationResult(result)
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
            return null
        }

        val jsonObject = JSONObject(jsonString)
        return jsonObject.getString("deepl_API_KEY")
    }
}
