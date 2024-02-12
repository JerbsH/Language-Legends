package com.example.languagelegends.database

import androidx.room.TypeConverter
import com.example.languagelegends.screens.Language
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}
