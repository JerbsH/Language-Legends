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

/**
 * This interface defines the callback methods for translation results and errors.
 */
interface TranslationCallback {
    fun onTranslationResult(result: String)
    fun onTranslationError(error: String)
}

/**
 * This class provides functionalities to translate text using the DeepL API.
 * It uses the OkHttp library to make network requests.
 */
class TranslateAPI(context: Context) {

    private val client = OkHttpClient()
    private val apiKey = getDeepLAPIKey(context)

    /**
     * Translates the given text to the target language.
     * @param text The text to be translated.
     * @param targetLanguage The target language for the translation.
     * @param callback The callback to be invoked when the translation result is received or when an error occurs.
     */
    fun translate(
        text: String?,
        targetLanguage: String,
        callback: TranslationCallback
    ) {
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body: RequestBody = "text=$text&target_lang=$targetLanguage".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api-free.deepl.com/v2/translate")
            .post(body)
            .addHeader("Authorization", "DeepL-Auth-Key $apiKey")
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
                val jsonObject = JSONObject(result)
                val translationsArray = jsonObject.getJSONArray("translations")
                val firstTranslationObject = translationsArray.getJSONObject(0)
                val translatedText = firstTranslationObject.getString("text")

                callback.onTranslationResult(translatedText)
            }
        })
    }

    /**
     * Retrieves the DeepL API key from a JSON file in the assets folder.
     * @param context The application context.
     * @return The DeepL API key, or null if an error occurs.
     */
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
