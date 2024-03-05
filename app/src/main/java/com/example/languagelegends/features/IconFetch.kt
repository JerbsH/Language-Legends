package com.example.languagelegends.features

// Function to get the correct country code for the flag icon
// Some language codes are wrong in the API, so we need to manually set them
// This function sets the correct country code that can be used in FlagKit (https://github.com/murgupluoglu/flagkit-android)

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
        "Esthonian" -> {
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
        } else -> {
            for (i in allLanguages) {
                if (i.key == language) {
                    flag = i.value.lowercase()
                }
            }
        }
    }
    return flag
}