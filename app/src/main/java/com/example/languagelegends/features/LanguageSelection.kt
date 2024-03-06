import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagelegends.R
import com.example.languagelegends.features.LANGUAGES

@Composable
fun LanguageSelection(onLanguageSelected: (String) -> Unit) {
    val context = LocalContext.current
    //deepl languages
    val languages = LANGUAGES

    var selectedOption by remember { mutableStateOf(languages.keys.first()) }
    var showDialog by remember { mutableStateOf(true) } // Set showDialog to true by default

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.select_language)) },
            text = {
                Box {
                    LazyColumn {
                        items(languages.keys.toList()) { language ->
                            Text(
                                text = language,
                                style = TextStyle(fontSize = 18.sp),
                                modifier = Modifier.clickable(
                                    onClick = {
                                        if (language != selectedOption) {
                                            selectedOption = language
                                            val sharedPreferences =
                                                context.getSharedPreferences(
                                                    "MyPrefs",
                                                    Context.MODE_PRIVATE
                                                )
                                            sharedPreferences.edit()
                                                .putString("selectedLanguageName", language)
                                                .putString("selectedLanguageCode", languages[language] ?: "EN")
                                                .apply()
                                            onLanguageSelected(language) // Pass the full language name
                                            showDialog = false
                                        }
                                    }
                                )
                            )
                            HorizontalDivider(modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.close))
                }
            },
            modifier = Modifier
                .fillMaxHeight(0.7f) // 70% of screen height
                .fillMaxWidth(0.7f) // 70% of screen width
        )
    }
}


