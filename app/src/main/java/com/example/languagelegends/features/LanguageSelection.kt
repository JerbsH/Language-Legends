package com.example.languagelegends.features

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagelegends.R
import com.murgupluoglu.flagkit.FlagKit
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.screens.Language
import com.example.languagelegends.screens.updateUserLanguages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userProfileDao: UserProfileDao = DatabaseProvider.getDatabase(application).userProfileDao()
    var selectedLanguage by mutableStateOf("English") // Default language
    var selectedLanguageIcon by mutableStateOf(icon(selectedLanguage)) // Default language icon

    fun updateLanguage(newLanguage: String) {
        viewModelScope.launch {
            val userProfile = withContext(Dispatchers.IO) {
                userProfileDao.getAllUserProfiles().firstOrNull()
            }
            if (userProfile != null) {
                val selectedLanguage = Language(newLanguage, 0, 0)
                userProfile.currentLanguage = selectedLanguage
                updateUserLanguages(userProfile, newLanguage) // Update languages
                userProfileDao.updateUserProfile(userProfile)
                this@UserProfileViewModel.selectedLanguage = newLanguage
                selectedLanguageIcon = icon(newLanguage) // Update language icon
            }
        }
    }
}
/*
class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userProfileDao: UserProfileDao = DatabaseProvider.getDatabase(application).userProfileDao()
    val selectedLanguage: String by mutableStateOf("English") // Default language
    var selectedLanguageIcon: String by mutableStateOf(icon(selectedLanguage)) // Default language icon

    fun updateLanguage(newLanguage: String) {
        viewModelScope.launch {
            val userProfile = withContext(Dispatchers.IO) {
                userProfileDao.getAllUserProfiles().firstOrNull()
            }
            if (userProfile != null) {
                val selectedLanguage = Language(newLanguage, 0, 0)
                userProfile.currentLanguage = selectedLanguage
                updateUserLanguages(userProfile, newLanguage) // Update languages
                userProfileDao.updateUserProfile(userProfile)
                selectedLanguageIcon = icon(newLanguage) // Update language icon
            }
        }
    }
}*/

@Composable
fun LanguageSelection(
    userProfileViewModel: UserProfileViewModel,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val languages = LANGUAGES

    var selectedOption by remember { mutableStateOf(languages.keys.first()) }
    var isDialogOpen by remember { mutableStateOf(true) } // Add this state to control the dialog visibility

    if (isDialogOpen) { // Show the dialog only if isDialogOpen is true
        AlertDialog(
            onDismissRequest = {
                isDialogOpen = false
            }, // Dismiss the dialog when outside is clicked
            title = { Text(text = stringResource(id = R.string.select_language)) },
            text = {
                Box {
                    LazyColumn {
                        items(languages.keys.toList()) { language ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (language != selectedOption) {
                                            selectedOption = language
                                            onLanguageSelected(language) // Pass the full language name

                                            // Update language in view model
                                            userProfileViewModel.viewModelScope.launch {
                                                userProfileViewModel.updateLanguage(language)
                                            }
                                        }
                                    }) {
                                Text(
                                    text = language,
                                    style = TextStyle(fontSize = 18.sp),
                                    modifier = Modifier
                                )
                                val flag = icon(language)
                                Image(
                                    painter = painterResource(FlagKit.getResId(context, flag)),
                                    contentDescription = "Flag of $language",
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(36.dp)
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDialogOpen = false
                    }, // Dismiss the dialog when the button is clicked
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)

                ) {
                    Text(
                        text = stringResource(id = R.string.close),
                    )
                }
            },
            modifier = Modifier
                .fillMaxHeight(0.7f) // 70% of screen height
                .fillMaxWidth(0.95f) // 70% of screen width
        )
    }
}