package com.example.languagelegends.features

/**
 * This data class represents a Language.
 * It holds the name of the language, the number of exercises done in this language,
 * the points earned in this language, the country code associated with this language,
 * and the timestamp of the last exercise done in this language.
 */
data class Language(
    val name: String,
    val countryCode: String,
    var exercisesDone: Int,
    var pointsEarned: Int,
    var exerciseTimestamp: Long = System.currentTimeMillis()
)

/**
 * This function returns the correct country code for the flag icon.
 * Some language codes are wrong in the API, so we need to manually set them.
 * This function sets the correct country code that can be used in FlagKit (https://github.com/murgupluoglu/flagkit-android).
 * @param language The name of the language.
 * @return The country code associated with the language.
 */
fun icon(language: String): String {
    val allLanguages = LANGUAGES
    var flag = ""
    when (language) {
        "English" -> {
            flag = "gb"
        }

        "Czech" -> {
            flag = "cz"
        }

        "Danish" -> {
            flag = "dk"
        }

        "Greek" -> {
            flag = "gr"
        }

        "English (British)" -> {
            flag = "gb"
        }

        "English (American)" -> {
            flag = "us"
        }

        "Estonian" -> {
            flag = "ee"
        }

        "Japanese" -> {
            flag = "jp"
        }

        "Korean" -> {
            flag = "kr"
        }

        "Norwegian" -> {
            flag = "no"
        }

        "Portuguese (Brazilian)" -> {
            flag = "br"
        }

        "Portuguese (European)" -> {
            flag = "pt"
        }

        "Slovenian" -> {
            flag = "si"
        }

        "Swedish" -> {
            flag = "se"
        }

        "Ukrainian" -> {
            flag = "ua"
        }

        "Chinese" -> {
            flag = "cn"
        }

        else -> {
            for (i in allLanguages) {
                if (i.key == language) {
                    flag = i.value.lowercase()
                }
            }
        }
    }
    return flag
}