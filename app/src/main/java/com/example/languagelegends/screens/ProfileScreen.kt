package com.example.languagelegends.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.languagelegends.database.Converters
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfile
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.features.ImagePickerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Language(val name: String, val exercisesDone: Int, val pointsEarned: Int)

@Composable
fun ProfileScreen(userProfileDao: UserProfileDao) {
    var username by remember { mutableStateOf("Matti") }
    var isEditingUsername by remember { mutableStateOf(true) }
    var selectedUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    Log.d("DBG", "Initial username: $username") // Log the initial username


    val context = LocalContext.current
    DatabaseProvider.getDatabase(context).userProfileDao()


    var image by remember { mutableStateOf<ByteArray?>(null) }
    val imageUri by remember { mutableStateOf<String?>(null) }

    // Fixed value for weeklyPoints
    var weeklyPoints = 1500

    if (selectedUserProfile == null) {
        weeklyPoints = 0
    }
    val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Fetch the user profile from the database when the ProfileScreen is shown
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userProfile = withContext(Dispatchers.IO) {
                userProfileDao.getAllUserProfiles().firstOrNull()
            }
            selectedUserProfile = userProfile
            image = userProfile?.image
            username = userProfile?.username ?: "Matti"
            weeklyPoints = userProfile?.weeklyPoints ?: 0
        }
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("DBG", "Image selected")
                val newImage = result.data?.getByteArrayExtra("image")

                // Check if selectedUserProfile is null
                if (selectedUserProfile == null) {
                    //create a new UserProfile with the current username
                    selectedUserProfile = UserProfile(
                        username = username,
                        currentLanguage = Language("English", 0, 0),
                        weeklyPoints = 1500
                    )
                }
                // Update the image of selectedUserProfile
                selectedUserProfile?.image = newImage

                // Update the user profile in the database
                coroutineScope.launch {
                    Log.d("DBG", "Updating user profile in database")
                    selectedUserProfile?.let { userProfileDao.updateUserProfile(it) }
                }
                // Update the image variable
                image = newImage
            }
        }

    // Update the user profile in the database when values change
    LaunchedEffect(
        image, imageUri,
        selectedUserProfile?.weeklyPoints,
        selectedUserProfile?.languages,
        selectedUserProfile?.currentLanguage,
        selectedUserProfile?.languagePoints,
        selectedUserProfile?.exercisesDone
    ) {
        selectedUserProfile?.let { userProfile ->
            coroutineScope.launch {
                userProfileDao.updateUserProfile(userProfile)
            }
            Log.d("DBG", "User profile updated in the database.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
        val converters = Converters()

        LaunchedEffect(selectedUserProfile, selectedUserProfile?.image) {
            val byteArray = selectedUserProfile?.image
            imageBitmap.value = if (byteArray != null) {
                converters.toBitmap(byteArray)?.asImageBitmap()
            } else {
                ImageBitmap(1, 1)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenHeightDp.dp * 1 / 5),
            horizontalArrangement = Arrangement.Center
        ) {
            if (imageBitmap.value != null) {
                Image(
                    bitmap = imageBitmap.value!!,
                    contentDescription = null,
                    modifier = Modifier
                        .size(
                            LocalConfiguration.current.screenWidthDp.dp * 1 / 2,
                            LocalConfiguration.current.screenHeightDp.dp * 1 / 5
                        )
                        .border(2.dp, Color.Green, shape = RoundedCornerShape(16.dp)),
                    alignment = Alignment.Center,

                    )
            }
        }
        val takePictureIntent = Intent(context, ImagePickerActivity::class.java).apply {
            putExtra("requestType", "camera")
            putExtra("username", username)
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue with your action
                pickImageLauncher.launch(takePictureIntent)
            } else {
                Log.d("DBG", "Camera permission is denied.")
            }
        }
        Text(
            text = "Select a profile picture",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

            Button(onClick = {
                Log.d("DBG", "Take Photo button clicked")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }) {
                Text("Take Photo")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                Log.d("DBG", "From Gallery button clicked")
                val pickImageIntent = Intent(context, ImagePickerActivity::class.java)
                pickImageIntent.putExtra("requestType", "gallery")
                pickImageIntent.putExtra("username", username)
                pickImageLauncher.launch(pickImageIntent)
            }) {
                Text("From Gallery")
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(thickness = 2.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome $username!",
                    textAlign = TextAlign.Center,
                )
                var editVisible by remember { mutableStateOf(false) }
                Button(onClick = {
                    editVisible = !editVisible
                }) {
                    Text(text = "Edit Username")
                }
                if (editVisible) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = username,
                            onValueChange = {
                                if (isEditingUsername) {
                                    username = it
                                }
                            },
                            label = { Text("Enter your username") },
                            enabled = isEditingUsername,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                Log.d("DBG", "Edit Username button clicked")
                                // Update UI
                                isEditingUsername = !isEditingUsername
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(if (isEditingUsername) "Select Username" else "Edit Username")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {}
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp)
                    .fillMaxWidth(),
                text = "Weekly Points: $weeklyPoints",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                    .fillMaxWidth(),
                text = "Total Points: ${selectedUserProfile?.languagePoints}",
                textAlign = TextAlign.Center
            )
        }
        // Display the list of learned languages
        Text(
            text = "Languages Learned:",
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

                    Log.d("ProfileScreen", "All users: $allUsers") // Log all users


                    if (allUsers.size == 1) {
                        // If there is exactly one user, update its username
                        val firstUser = allUsers.first()
                        firstUser.username = username
                        updateUserLanguages(firstUser)
                        userProfileDao.updateUserProfile(firstUser)
                        Log.d("DBG", "Updating user profile in the database.")

                        // Fetch the updated user profile from the database
                        val updatedUserProfile = withContext(Dispatchers.IO) {
                            userProfileDao.getAllUserProfiles()
                                .firstOrNull { it.username == firstUser.username }
                        }
                        // Update selectedUserProfile
                        selectedUserProfile = updatedUserProfile
                    } else {
                        // If the database is empty or has more than one user, create a new user profile
                        val newUserProfile = UserProfile(
                            username = username,
                            weeklyPoints = 1500,
                            currentLanguage = Language("English", 0, 0),
                            languagePoints = 0
                        )
                        updateUserLanguages(newUserProfile)
                        userProfileDao.insertUserProfile(newUserProfile)
                        Log.d(
                            "ProfileScreen",
                            "New user profile created and added to the database."
                        )

                        // Fetch the updated user profile from the database
                        val updatedUserProfile = withContext(Dispatchers.IO) {
                            userProfileDao.getAllUserProfiles()
                                .firstOrNull { it.username == newUserProfile.username }
                        }
                        // Update selectedUserProfile
                        selectedUserProfile = updatedUserProfile
                    }
                }
            } catch (e: Exception) {
                Log.e("DBG", "Error updating user profile: ${e.message}", e)
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
                        Text("Points earned: ${language.pointsEarned}", color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isDialogOpen = false }) {
                            Text("Close")
                        }
                    }
                })
            }
        }
        // Button to clear database at the bottom
        Button(onClick = {
            Log.d("DBG", "Clear Database button clicked")
            coroutineScope.launch {
                userProfileDao.clearDatabase()
                selectedUserProfile = null
            }
        }) {
            Text("Clear Database")
        }

    }
}

fun updateUserLanguages(userProfile: UserProfile) {
    val updatedLanguages = listOf(
        Language("English", 50, 3000), Language("Spanish", 50, 500), Language("French", 70, 12000)
    )
    userProfile.languages = updatedLanguages
    userProfile.exercisesDone = updatedLanguages.sumOf { it.exercisesDone }
    userProfile.languagePoints = updatedLanguages.sumOf { it.pointsEarned }
}

@Composable
fun LanguageItem(language: Language, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = language.name)
    }
}