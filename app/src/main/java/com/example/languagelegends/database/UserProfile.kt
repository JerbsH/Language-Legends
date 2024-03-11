package com.example.languagelegends.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    var username: String,
    var weeklyPoints: Int,
    var languages: MutableList<Language> = mutableListOf(),
    var currentLanguage: Language,
    var languagePoints: Int = 0,
    var exercisesDone: Int? = 0,
    var image: ByteArray? = null,
    var created: Int?
)