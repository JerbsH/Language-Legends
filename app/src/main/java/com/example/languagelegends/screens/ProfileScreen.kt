package com.example.languagelegends.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import com.example.languagelegends.R
import com.example.languagelegends.aicomponents.AiChatViewModel
import com.example.languagelegends.database.Converters
import com.example.languagelegends.database.UserProfile
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.features.ImagePickerActivity
import com.example.languagelegends.features.LANGUAGES
import com.example.languagelegends.features.Language
import com.example.languagelegends.features.UserProfileViewModel
import com.example.languagelegends.features.icon
import com.murgupluoglu.flagkit.FlagKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This is the main screen for the user profile. It displays the user's profile picture, username,
 * weekly points, total points, and the languages they are learning. The user can also edit their
 * username, select a new profile picture, and clear the database from this screen.
 *
 * @param userProfileDao The DAO for accessing the UserProfile in the database.
 * @param apiSelectedLanguage The language selected by the user.
 * @param onBottomBarVisibilityChanged A function to change the visibility of the bottom bar.
 * @param userProfileViewModel The ViewModel for the UserProfile.
 * @param aiChatViewModel The ViewModel for the AI chat.
 */
@Composable
fun ProfileScreen(
    userProfileDao: UserProfileDao,
    apiSelectedLanguage: String,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    userProfileViewModel: UserProfileViewModel,
    aiChatViewModel: AiChatViewModel
) {
    aiChatViewModel.chatVisible.value = false
    var username by remember { mutableStateOf("") }
    var isEditingUsername by remember { mutableStateOf(true) }
    var selectedUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    val countryCode by remember(selectedLanguage) {
        mutableStateOf(
            LANGUAGES[selectedLanguage?.name] ?: "EN-GB"
        )
    }
    var isDialogOpen by remember { mutableStateOf(false) }
    var created by remember { mutableIntStateOf(0) }
    var weeklyPoints by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    var image by remember { mutableStateOf<ByteArray?>(null) }
    val imageUri by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch the user profile from the database when the ProfileScreen is shown
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userProfile = withContext(Dispatchers.IO) {
                userProfileDao.getAllUserProfiles().firstOrNull()
            }
            selectedUserProfile = userProfile
            image = userProfile?.image
            username = userProfile?.username ?: ""

            // Calculate weeklyPoints based on exercise timestamps
            val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
            weeklyPoints = userProfile?.languages?.filter { it.exerciseTimestamp >= oneWeekAgo }
                ?.sumOf { it.pointsEarned } ?: 0
            created = userProfile?.created ?: 0
            selectedLanguage = userProfile?.currentLanguage
            userProfile?.weeklyPoints = weeklyPoints

            // Update the UserProfile in the database
            withContext(Dispatchers.IO) {
                if (userProfile != null) {
                    userProfileDao.updateUserProfile(userProfile)
                }
            }
        }
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newImage = result.data?.getByteArrayExtra("image")

                if (selectedUserProfile == null) {
                    //create a new UserProfile with the current username
                    selectedUserProfile = UserProfile(
                        username = username,
                        currentLanguage = selectedLanguage ?: Language("No selection", "", 0, 0, 0),
                        weeklyPoints = 0,
                        created = 1,
                        pointsEarned = 0
                    )
                }
                // Update the image of selectedUserProfile
                selectedUserProfile?.image = newImage
                // Update the user profile in the database
                coroutineScope.launch {
                    selectedUserProfile?.let { userProfileDao.updateUserProfile(it) }
                }
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
        selectedUserProfile?.exercisesDone,
        selectedUserProfile?.created
    ) {
        selectedUserProfile?.let { userProfile ->
            val newLanguage = if (apiSelectedLanguage.isNotEmpty()) {
                Language(apiSelectedLanguage, countryCode, 0, 0, 0)
            } else {
                selectedLanguage ?: Language("Default", countryCode, 0, 0, 0)
            }
            userProfile.currentLanguage = newLanguage
            if (apiSelectedLanguage != userProfile.currentLanguage.name) {
                userProfile.currentLanguage = Language(apiSelectedLanguage, countryCode, 0, 0, 0)

                // Check if the language is already in the list
                val existingLanguage = userProfile.languages.find { it.name == apiSelectedLanguage }
                if (existingLanguage != null) {
                    // Update the existing language
                    existingLanguage.exercisesDone = 0
                    existingLanguage.pointsEarned = 0
                } else {
                    userProfile.languages.add(Language(apiSelectedLanguage, countryCode, 0, 0, 0))
                }
                coroutineScope.launch {
                    userProfileDao.updateUserProfile(userProfile)
                    updateUserLanguages(
                        userProfile,
                        apiSelectedLanguage
                    )
                }
            }
        }
    }
    /**
     * This function is responsible for displaying the user's profile.
     * It shows the user's profile picture, username, and the list of languages they are learning.
     * It also provides options to take a new profile picture, edit the username, and clear the database.
     */
    @Composable
    fun showProfile() {
        onBottomBarVisibilityChanged(true)
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
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.Green, shape = RoundedCornerShape(16.dp))
                            .size(
                                LocalConfiguration.current.screenWidthDp.dp * 1 / 2,
                                LocalConfiguration.current.screenHeightDp.dp * 1 / 5
                            ),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
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
                    pickImageLauncher.launch(takePictureIntent)
                } else {
                    Log.d("DBG", "Camera permission is denied.")
                }
            }
            Text(
                text = stringResource(id = R.string.select_picture),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                Button(onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text(stringResource(id = R.string.take_photo))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    val pickImageIntent = Intent(context, ImagePickerActivity::class.java)
                    pickImageIntent.putExtra("requestType", "gallery")
                    pickImageIntent.putExtra("username", username)
                    pickImageLauncher.launch(pickImageIntent)
                }) {
                    Text(stringResource(id = R.string.from_gallery))
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(thickness = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.welcome, username),
                        textAlign = TextAlign.Center,
                    )
                    var editVisible by remember { mutableStateOf(false) }
                    Button(onClick = {
                        editVisible = !editVisible
                    }) {
                        Text(
                            text = if (editVisible) stringResource(id = R.string.close) else stringResource(
                                id = R.string.edit_username
                            )
                        )
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
                                label = { Text(stringResource(id = R.string.enter_name)) },
                                enabled = isEditingUsername,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    isEditingUsername = !isEditingUsername
                                },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Text(
                                    if (isEditingUsername) stringResource(id = R.string.select_username) else stringResource(
                                        id = R.string.edit_username
                                    )
                                )
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
                    text = stringResource(id = R.string.weekly_points, weeklyPoints),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                        .fillMaxWidth(),
                    text = stringResource(
                        id = R.string.total_points,
                        selectedUserProfile?.languagePoints ?: 0
                    ),
                    textAlign = TextAlign.Center
                )
            }
            // Display the list of learned languages
            Text(
                text = stringResource(id = R.string.lang_learned),
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LaunchedEffect(isEditingUsername, username) {
                try {
                    if (!isEditingUsername) {
                        val allUsers = withContext(Dispatchers.IO) {
                            userProfileDao.getAllUserProfiles()
                        }

                        if (allUsers.size == 1) {
                            val firstUser = allUsers.first()
                            firstUser.username = username
                            updateUserLanguages(
                                firstUser,
                                selectedLanguage?.name ?: "Default Language"
                            )
                            userProfileDao.updateUserProfile(firstUser)
                            val updatedUserProfile = withContext(Dispatchers.IO) {
                                userProfileDao.getAllUserProfiles()
                                    .firstOrNull { it.username == firstUser.username }
                            }
                            selectedUserProfile = updatedUserProfile

                        } else {
                            // If the database is empty or has more than one user, create a new user profile
                            val newUserProfile = UserProfile(
                                username = username,
                                weeklyPoints = 0,
                                currentLanguage = selectedLanguage ?: Language(
                                    "No selection",
                                    countryCode,
                                    0,
                                    0,
                                    0
                                ),

                                languagePoints = 0,
                                created = 1,
                                pointsEarned = 0
                            )
                            updateUserLanguages(
                                newUserProfile,
                                selectedLanguage?.name ?: "Default Language"
                            )
                            userProfileDao.insertUserProfile(newUserProfile)
                            // Fetch the updated user profile from the database
                            val updatedUserProfile = withContext(Dispatchers.IO) {
                                userProfileDao.getAllUserProfiles()
                                    .firstOrNull { it.username == newUserProfile.username }
                            }
                            // Update selectedUserProfile
                            selectedUserProfile = updatedUserProfile
                        }
                        // Calculate weeklyPoints based on exercise timestamps
                        val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                        weeklyPoints =
                            selectedUserProfile?.languages?.filter { it.exerciseTimestamp >= oneWeekAgo }
                                ?.sumOf { it.pointsEarned } ?: 0
                    }

                } catch (e: Exception) {
                    Log.e("DBG", "Error updating user profile: ${e.message}", e)
                }
            }
            LaunchedEffect(apiSelectedLanguage) {
                coroutineScope.launch {
                    val updatedUserProfile = withContext(Dispatchers.IO) {
                        userProfileDao.getAllUserProfiles().firstOrNull()
                    }
                    selectedUserProfile = updatedUserProfile
                }
            }
            // Display the list of languages from the updated user profile
            Box(modifier = Modifier.height(100.dp)) {
                // Use LazyColumn to display the list of languages
                LazyColumn {
                    items(selectedUserProfile?.languages ?: emptyList()) { language ->
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
                            Text(
                                stringResource(id = R.string.for_language, language.name),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(
                                    id = R.string.exercises_done,
                                    language.exercisesDone
                                ), color = Color.Black
                            )
                            Text(
                                stringResource(id = R.string.points_earned, language.pointsEarned),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { isDialogOpen = false }) {
                                Text(stringResource(id = R.string.close))
                            }
                        }
                    })
                }
            }
            // Button to clear database at the bottom
            Button(onClick = {
                coroutineScope.launch {
                    userProfileDao.clearDatabase()
                    selectedUserProfile = null
                }
            }) {
                Text("Clear Database")
            }

        }
    }

    /**
     * This function is responsible for displaying the initial screen where the user can enter their name and select a language to learn.
     * It validates the user's input and creates a new user profile in the database.
     * It also updates the selected language in the view model.
     */
    @Composable
    fun showNameScreen(userProfileViewModel: UserProfileViewModel) {
        onBottomBarVisibilityChanged(false)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.welcome_to_language_legends),
                textAlign = TextAlign.Center,
                fontSize = 22.sp
            )
            Text(
                text = stringResource(R.string.enter_name_to_start),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TextField(
                    value = username,
                    onValueChange = {
                        if (isEditingUsername) {
                            username = it
                        }
                    },
                    label = { Text(stringResource(id = R.string.enter_name)) },
                    enabled = isEditingUsername,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.select_language_to_learn),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            var selection by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .height(190.dp)
            ) {
                LazyColumn {
                    items(LANGUAGES.keys.toList()) { language ->
                        Card(
                            border = if (selection == language) {
                                BorderStroke(2.dp, Color.Green)
                            } else {
                                BorderStroke(0.dp, Color.White)
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    selectedLanguage = Language(language, countryCode, 0, 0, 0)
                                    selection = language
                                }
                        ) {
                            Row {
                                Text(
                                    text = language,
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = 0.5f)
                                        .padding(8.dp)
                                )
                                val flag = icon(language)
                                Image(
                                    painter = painterResource(
                                        FlagKit.getResId(
                                            context,
                                            flag
                                        )
                                    ),
                                    contentDescription = "Flag of $language",
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (username.isEmpty()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_choose_username),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else if (username.length < 2) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.username_min_length_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (username.length > 20) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.username_max_length_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (selectedLanguage == null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_choose_language),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        isEditingUsername = !isEditingUsername
                        created = 1

                        coroutineScope.launch {
                            val newUserProfile = UserProfile(
                                username = username,
                                currentLanguage = selectedLanguage ?: Language(
                                    "English",
                                    countryCode,
                                    0,
                                    0,
                                    0
                                ),
                                weeklyPoints = 0,
                                created = created,
                                pointsEarned = 0
                            )
                            // Insert the new UserProfile into the database
                            userProfileDao.insertUserProfile(newUserProfile)
                            // Update selectedUserProfile
                            selectedUserProfile = newUserProfile
                            // Update language in view model
                            userProfileViewModel.viewModelScope.launch {
                                userProfileViewModel.updateLanguage(
                                    selectedLanguage?.name ?: "English"
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.size(114.dp)

            ) {
                Text(
                    text = stringResource(R.string.start_adventure),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    if (created == 1) {
        showProfile()
    } else if (username.isEmpty() || isEditingUsername) {
        showNameScreen(userProfileViewModel)
    }

}

/**
 * This function updates the languages of a user profile. If the selected language already exists in the
 * user's languages, it resets the exercises done and points earned for that language. If the selected
 * language does not exist in the user's languages, it adds the language to the list.
 * @param userProfile The UserProfile to update.
 * @param selectedLanguage The selected language.
 */
fun updateUserLanguages(userProfile: UserProfile, selectedLanguage: String) {
    val existingLanguage = userProfile.languages.find { it.name == selectedLanguage }
    val countryCode = LANGUAGES[selectedLanguage] ?: ""
    if (existingLanguage != null) {
        existingLanguage.exercisesDone = 0
        existingLanguage.pointsEarned = 0
    } else {
        userProfile.languages.add(Language(selectedLanguage, countryCode, 0, 0, 0))
    }
    userProfile.exercisesDone = userProfile.languages.sumOf { it.exercisesDone }
    userProfile.languagePoints = userProfile.languages.sumOf { it.pointsEarned }
    userProfile.currentLanguage = Language(selectedLanguage, countryCode, 0, 0)

}

/**
 * This is a composable function that displays a language item. It is a row with the name of the language.
 * When the row is clicked, it calls the onClick function.
 * @param language The Language to display.
 * @param onClick The function to call when the row is clicked.
 */
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
