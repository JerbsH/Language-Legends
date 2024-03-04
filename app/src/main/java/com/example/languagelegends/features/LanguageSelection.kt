
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun LanguageSelection(onLanguageSelected: (String) -> Unit) {
    val context = LocalContext.current
    val languages = mapOf(
        "Bulgarian" to "BG",
        "Czech" to "CS",
        "Danish" to "DA",
        "German" to "DE",
        "Greek" to "EL",
        "English (British)" to "EN-GB",
        "English (American)" to "EN-US",
        "Spanish" to "ES",
        "Estonian" to "ET",
        "Finnish" to "FI",
        "French" to "FR",
        "Hungarian" to "HU",
        "Indonesian" to "ID",
        "Italian" to "IT",
        "Japanese" to "JA",
        "Korean" to "KO",
        "Lithuanian" to "LT",
        "Latvian" to "LV",
        "Norwegian (Bokmål)" to "NB",
        "Dutch" to "NL",
        "Polish" to "PL",
        "Portuguese (Brazilian)" to "PT-BR",
        "Portuguese (European)" to "PT-PT",
        "Romanian" to "RO",
        "Russian" to "RU",
        "Slovak" to "SK",
        "Slovenian" to "SL",
        "Swedish" to "SV",
        "Turkish" to "TR",
        "Ukrainian" to "UK",
        "Chinese (simplified)" to "ZH"
    )

    var selectedOption by rememberSaveable { mutableStateOf(languages.keys.first()) }

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {
                if (it in languages.keys) {
                    selectedOption = it
                    Log.d("DBG", "Selected option: $selectedOption")
                }
            },
            label = { Text("Select a language") },
            trailingIcon = {
                IconButton(onClick = { selectedOption = "" }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown arrow"
                    )
                }
            }
        )
        if (selectedOption == "") {
            Column {
                languages.keys.forEach { language ->
                    Text(
                        text = language,
                        modifier = Modifier.clickable {
                            selectedOption = language
                            val sharedPreferences =
                                context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit()
                                .putString("selectedLanguage", languages[language]).apply()
                            onLanguageSelected(languages[language] ?: "EN")
                        }
                    )
                }
            }
        }
    }
}
