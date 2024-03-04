
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun LanguageSelection(onLanguageSelected: (String) -> Unit) {
    val languages = listOf(
        "German",
        "English",
        "French",
        "Spanish",
        "Italian",
        "Dutch",
        "Polish",
        "Portuguese",
        "Russian",
        "Japanese",
        "Chinese"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(languages.first()) }

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { selectedOption = it },
            label = { Text("Select a language") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown arrow"
                    )
                }
            }
        )

        if (expanded) {
            LazyColumn {
                itemsIndexed(languages) { _, language ->
                    DropdownMenuItem(
                        onClick = {
                            selectedOption = language
                            expanded = false
                            onLanguageSelected(language)
                        },
                        text = { Text(language) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}
