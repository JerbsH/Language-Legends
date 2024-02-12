package com.example.languagelegends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.languagelegends.R

data class Language(val name: String, val exercisesDone: Int)

@Composable
fun ProfileScreen() {
    var username by remember { mutableStateOf("Matti") }

    // Sample data for languages and exercises
    val languages = listOf(
        Language("English", 10),
        Language("Spanish", 5),
        Language("French", 7)
    )

    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder image
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 30.dp)
                .size(120.dp),
        )

        Text(
            text = "Username:",
            color = Color.Black,
        )

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter your username") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of learned languages
        Text(
            text = "Languages Learned:",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Display the list of languages
        LazyColumn {
            items(languages) { language ->
                LanguageItem(language = language) {
                    selectedLanguage = language
                    isDialogOpen = true
                }
            }
        }

        // Dialog to display the number of exercises done
        selectedLanguage?.let { language ->
            if (isDialogOpen) {
                Dialog(
                    onDismissRequest = { isDialogOpen = false },
                    content = {
                        Column(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Text("Exercises for ${language.name}", color = Color.Black)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Exercises done: ${language.exercisesDone}",
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { isDialogOpen = false }) {
                                Text("Close")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageItem(language: Language, onClick: () -> Unit) {
    Text(
        text = language.name,
        modifier = Modifier.clickable(onClick = onClick)
    )
}
