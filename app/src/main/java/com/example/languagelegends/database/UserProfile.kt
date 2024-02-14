package com.example.languagelegends.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.languagelegends.screens.Language

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    var username: String,
    var weeklyPoints: Int,
    var languages: List<Language> = emptyList(),
    var currentLanguage: Language,
    var languagePoints: Int = 0,
    var exercisesDone: Int? = 0,
    var image: ByteArray? = null,
)