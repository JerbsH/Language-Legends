package com.example.languagelegends.database

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.example.languagelegends.features.Language

/**
 * This data class represents a UserProfile.
 * It holds the user's id, username, weekly points, list of languages, current language,
 * language points, number of exercises done, user's image, creation timestamp,
 * points earned, and the timestamp of the last exercise.
 */
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
    var completedLevels: Int = 0,
    var image: ByteArray? = null,
    var created: Int?,
    var pointsEarned: Int,
    var exerciseTimestamp: Long = 0L

)