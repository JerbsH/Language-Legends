package com.example.languagelegends.features

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.languagelegends.R
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.screens.ViewState
import com.example.languagelegends.screens.updateUserLanguages
import com.murgupluoglu.flagkit.FlagKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userProfileDao: UserProfileDao =
        DatabaseProvider.getDatabase(application).userProfileDao()
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("LanguageLegends", Context.MODE_PRIVATE)

    var selectedLanguage by mutableStateOf("") // Default language
    var selectedLanguageIcon by mutableStateOf("") // Default language icon
    val selectedLanguageLiveData = MutableLiveData<String>()

    init {
        loadSelectedLanguage()
    }

    fun updateLanguage(newLanguage: String, viewState: ViewState) {
        Log.d("DBG", "updateLanguage called")
        viewModelScope.launch {
            Log.d("DBG", "UserProfileViewModel: Updating language to: $newLanguage")

            try {
                val userProfile = withContext(Dispatchers.IO) {
                    userProfileDao.getAllUserProfiles().firstOrNull()
                }
                if (userProfile == null) {
                    Log.e("DBG", "No user profile found")
                    return@launch
                }

                val currentLanguage = userProfile.currentLanguage

                // Save the current points and exercises done
                currentLanguage.let {
                    it.pointsEarned = it.pointsEarned
                    it.exercisesDone = it.exercisesDone
                }

                // Find the language from 'languages', if it's present save on top of it
                val indexSelected = userProfile.languages.indexOfFirst { it.name == userProfile.currentLanguage.name }
                if (indexSelected != -1) {
                    userProfile.languages[indexSelected] = currentLanguage
                }

                var selectedLanguage =
                    Language(
                        name = newLanguage,
                        exercisesDone = 0,
                        pointsEarned = 0,
                        exerciseTimestamp = System.currentTimeMillis(),
                        countryCode = ""
                    )

                val languagesSelected = userProfile.languages.find { it.name == newLanguage }
                if (languagesSelected != null) {
                    selectedLanguage = languagesSelected
                } else {
                    userProfile.languages.add(selectedLanguage)
                }
                viewState.setCompletedExercises(selectedLanguage.exercisesDone)

                userProfile.currentLanguage = selectedLanguage
                // This will just reset everything back to 0 updateUserLanguages(userProfile, newLanguage) // Update languages
                userProfileDao.updateUserProfile(userProfile)
                // Save the selected language to SharedPreferences
                sharedPreferences.edit().putString("selectedLanguage", newLanguage).apply()
                Log.d("dbg", "updatelanguage Updated selectedLanguageLiveData to: $newLanguage")

                // Update the ViewModel's selectedLanguage and selectedLanguageIcon
                this@UserProfileViewModel.selectedLanguage = newLanguage
                selectedLanguageIcon = icon(newLanguage)
                selectedLanguageLiveData.value = newLanguage
            } catch (e: Exception) {
                Log.e("DBG", "Error updating language: ${e.message}")
            }
        }
    }

    private fun loadSelectedLanguage() {
        selectedLanguage = sharedPreferences.getString("selectedLanguage", "English") ?: "English"
        Log.d("dbg", "UserProfileViewModel: Loaded selected language: $selectedLanguage")
        selectedLanguageIcon = icon(selectedLanguage)
    }

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
                            color = Color.Black,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxHeight(0.7f) // 70% of screen height
                    .fillMaxWidth(0.95f) // 70% of screen width
            )
        }
    }
}
