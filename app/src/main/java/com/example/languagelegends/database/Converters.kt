package com.example.languagelegends.database

import androidx.room.TypeConverter
import com.example.languagelegends.screens.Language
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class Converters {

    @TypeConverter
    fun fromLanguageList(value: String?): List<Language>? {
        val listType = object : TypeToken<List<Language>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toLanguageList(list: List<Language>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromLanguage(value: String): Language {
        val type = object : TypeToken<Language>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun languageToString(language: Language): String {
        return Gson().toJson(language)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size ?: 0)
    }
}
