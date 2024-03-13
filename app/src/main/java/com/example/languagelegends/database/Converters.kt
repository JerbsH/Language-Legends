package com.example.languagelegends.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.languagelegends.features.Language

/**
 * This class provides methods to convert between different data types.
 * It is used by Room to convert complex data types when persisting to and reading from the database.
 */
class Converters {

    private val gson = Gson()

    /**
     * Converts a list of Language objects to a JSON string.
     */
    @TypeConverter
    fun fromLanguageList(value: MutableList<Language>?): String {
        return gson.toJson(value)
    }

    /**
     * Converts a JSON string to a list of Language objects.
     */
    @TypeConverter
    fun toLanguageList(value: String): MutableList<Language>? {
        val type = object : TypeToken<MutableList<Language>>() {}.type
        return gson.fromJson(value, type)
    }

    /**
     * Converts a JSON string to a Language object.
     */
    @TypeConverter
    fun fromLanguage(value: String): Language {
        val type = object : TypeToken<Language>() {}.type
        return Gson().fromJson(value, type)
    }

    /**
     * Converts a Language object to a JSON string.
     */
    @TypeConverter
    fun languageToString(language: Language): String {
        return Gson().toJson(language)
    }

    /**
     * Converts a Bitmap object to a ByteArray.
     * The Bitmap is compressed to JPEG format.
     * The quality of the Bitmap is reduced if the size of the ByteArray exceeds 1MB.
     */
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        val maxSize = 720 * 720
        var quality = 70
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        while (stream.toByteArray().size > maxSize) {
            stream.reset()
            quality -= 5
            bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }
        return stream.toByteArray()
    }

    /**
     * Converts a ByteArray to a Bitmap object.
     */
    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size ?: 0)
    }

}
