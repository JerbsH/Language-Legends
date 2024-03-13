package com.example.languagelegends.database

/**
 * This data class represents a Language.
 * It holds the name of the language, the number of exercises done in this language,
 * the points earned in this language, and the country code associated with this language.
 */
data class Language(
    val name: String,
    var exercisesDone: Int,
    var pointsEarned: Int,
    val countryCode: String,

    )