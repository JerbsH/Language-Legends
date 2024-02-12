package com.example.languagelegends.screens

import android.util.Log
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
import com.example.languagelegends.database.UserProfile
import com.example.languagelegends.database.UserProfileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Language(val name: String, val exercisesDone: Int)

@Composable
fun ProfileScreen(userProfileDao: UserProfileDao) {
    var username by remember { mutableStateOf("Matti") }
    var isEditingUsername by remember { mutableStateOf(true) }
    var selectedUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }

    // Fixed value for weeklyPoints
    val weeklyPoints = 1500

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        val coroutineScope = rememberCoroutineScope()

        // Add this button in your Column
        Button(onClick = {
            coroutineScope.launch {
                userProfileDao.clearDatabase()
            }
        }) {
            Text("Clear Database")
        }
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(value = username, onValueChange = {
                if (isEditingUsername) {
                    username = it
                }
            }, label = { Text("Enter your username") }, enabled = isEditingUsername
            )

            Button(onClick = {
                // Update UI
                isEditingUsername = !isEditingUsername
            }) {
                Text(if (isEditingUsername) "Select Username" else "Edit Username")
            }
        }

        // Display the fixed value for weeklyPoints
        Text(
            text = "Weekly Points: $weeklyPoints",
            color = Color.Black,
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


        // Fetch user profile from the database using a coroutine
        LaunchedEffect(isEditingUsername, username) {
            try {
                if (!isEditingUsername) {
                    // User not found, check if the database is empty
                    val allUsers = withContext(Dispatchers.IO) {
                        userProfileDao.getAllUserProfiles()
                    }

                    if (allUsers.size == 1) {
                        // If there is exactly one user, update its username
                        val firstUser = allUsers.first()
                        firstUser.username = username
                        updateUserLanguages(firstUser)
                        userProfileDao.updateUserProfile(firstUser)
                        Log.d("ProfileScreen", "Updating user profile in the database.")

                        // Update selectedUserProfile
                        selectedUserProfile = firstUser
                    } else {
                        // If the database is empty or has more than one user, create a new user profile
                        val newUserProfile = UserProfile(username = username, weeklyPoints = 1500)
                        updateUserLanguages(newUserProfile)
                        userProfileDao.insertUserProfile(newUserProfile)
                        Log.d(
                            "ProfileScreen",
                            "New user profile created and added to the database."
                        )

                        // Update selectedUserProfile
                        selectedUserProfile = newUserProfile
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error updating user profile: ${e.message}", e)
            }
        }

// Display the list of languages from the user profile
        LazyColumn {
            selectedUserProfile?.languages?.let { languages ->
                items(languages) { language ->
                    LanguageItem(language = language) {
                        selectedLanguage = language
                        isDialogOpen = true
                    }
                }
            }
        }

        // Dialog to display the number of exercises done
        selectedLanguage?.let { language ->
            if (isDialogOpen) {
                Dialog(onDismissRequest = { isDialogOpen = false }, content = {
                    Column(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Text("Exercises for ${language.name}", color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Exercises done: ${language.exercisesDone}", color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isDialogOpen = false }) {
                            Text("Close")
                        }
                    }
                })
            }
        }
    }
}

fun updateUserLanguages(userProfile: UserProfile) {
    val updatedLanguages = listOf(
        Language("English", 50), Language("Spanish", 50), Language("French", 70)
    )
    userProfile.languages = updatedLanguages
    userProfile.exercisesDone = updatedLanguages.sumOf { it.exercisesDone }
}

@Composable
fun LanguageItem(language: Language, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Text(text = language.name)
    }
}

