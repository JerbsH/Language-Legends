package com.example.languagelegends.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.languagelegends.OnCompleteExercise
import com.example.languagelegends.R
import com.example.languagelegends.aicomponents.AiChatViewModel
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfile
import com.example.languagelegends.database.UserProfileDao
import com.example.languagelegends.features.SensorHelper
import com.example.languagelegends.features.UserProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Move points to a constant value
private const val POINTS_PER_EXERCISE = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(navController: NavController, apiSelectedLanguage: String, aiChatViewModel: AiChatViewModel, onCompleteExercise: OnCompleteExercise) {
    var currentExercise by remember { mutableIntStateOf(1) }
    aiChatViewModel.chatVisible.value = false

    // Define userProfileDao and exerciseTimestamp here
    val context = LocalContext.current
    val userProfileDao = DatabaseProvider.getDatabase(context).userProfileDao()
    val exerciseTimestamp = System.currentTimeMillis()

    //Define the layout for the exercises
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        // Display the appropriate exercise based on the currentExercise state
        when (currentExercise) {
            1 -> {
                WordScrambleExercise(
                    onNextExercise = {
                        currentExercise++ // Move to the next exercise
                    },
                    onGoBack = { navController.navigate("path") },
                    sensorHelper = SensorHelper(LocalContext.current),
                    userProfileDao = userProfileDao,

                    )
            }

            2 -> {
                SecondExercise(
                    onNextExercise = {
                        currentExercise++
                    },
                    onGoBack = { navController.navigate("path") },
                    userProfileDao = userProfileDao,
                )
            }

            3 -> {
                TiltExercise(
                    sensorHelper = SensorHelper(LocalContext.current),
                    onExerciseCompleted = {
                        currentExercise++
                        onCompleteExercise()
                    },
                    onGoBack = { navController.navigate("path") },
                    userProfileDao = userProfileDao,
                )
            }
            // Add more exercises as needed
            else -> {
                Column {
                    TopAppBar(
                        title = { Text(text = "") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate("path") }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                            }
                        }
                    )
                    Text(
                        text = stringResource(id = R.string.all_exercises),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.handshake),
                        contentDescription = "High Five Icon",
                        modifier = Modifier
                            .size(100.dp) // adjust the size as needed
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScrambleExercise(
    onNextExercise: () -> Unit,
    onGoBack: () -> Unit,
    sensorHelper: SensorHelper, // Pass the sensor helper instance
    userProfileDao: UserProfileDao,
    userProfileViewModel: UserProfileViewModel = viewModel(),

    ) {
    // List of words for the exercise
    val wordList = remember {
        listOf(
            "apple",
            "banana",
            "orange",
            "grape",
            "strawberry"
        )
    }

    // State to control the visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }

    // Randomly select a word from the list
    val currentWord = remember {
        wordList.random()
    }

    // State to hold the shuffled letters of the current word
    var shuffledLetters by remember {
        mutableStateOf(
            currentWord.toCharArray().toList().shuffled()
        )
    }

    // Ensure the shuffled word is never the same as the original word
    while (shuffledLetters.joinToString("") == currentWord) {
        shuffledLetters = currentWord.toCharArray().toList().shuffled()
    }

    // Register shake detection when the composable is launched
    LaunchedEffect(Unit) {
        sensorHelper.setShakeListener {
            // Shuffle the letters when the device is shaken
            shuffledLetters = currentWord.toCharArray().toList().shuffled()
            // Ensure the shuffled word is never the same as the original word
            while (shuffledLetters.joinToString("") == currentWord) {
                shuffledLetters = currentWord.toCharArray().toList().shuffled()
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            // Unregister sensor listener when the composable is disposed
            sensorHelper.unregisterSensorListener()
        }
    }

    // State to track user input for the unscrambled word
    var userInput by remember { mutableStateOf("") }

    // State to track if the user's input is correct
    var isCorrect by remember { mutableStateOf(false) }

    val points = POINTS_PER_EXERCISE

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.exercise_1)) },
            navigationIcon = {
                IconButton(onClick = { onGoBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Display the hint
            Text(
                text = stringResource(id = R.string.shuffle_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            val image =
                when (currentWord) {
                    "apple" -> R.drawable.apple
                    "banana" -> R.drawable.banana
                    "orange" -> R.drawable.orange
                    "grape" -> R.drawable.grape
                    "strawberry" -> R.drawable.strawberry
                    else -> R.drawable.fruit
                }
            // Display the picture of fruits
            Image(
                painter = painterResource(id = image),
                contentDescription = "Fruits",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.unscramble_word),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Display the shuffled letters
            Text(
                text = shuffledLetters.joinToString(separator = " "),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Text field for user input
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text(text = stringResource(id = R.string.your_answer)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Check if the user input matches the original word
            isCorrect = userInput.equals(currentWord, ignoreCase = true)

            // Display feedback based on user input
            if (userInput.isNotEmpty()) {
                Text(
                    text = if (isCorrect) stringResource(id = R.string.correct) else stringResource(
                        id = R.string.keep_trying
                    ),
                    color = if (isCorrect) Color.Black else Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Continue button (enabled only if the answer is correct)
            Button(
                onClick = {
                    if (isCorrect) {
                        // Update points and show dialog
                        userProfileViewModel.viewModelScope.launch {
                            val userProfile = updatePointsAndProceed(userProfileDao, points, userProfileViewModel)
                            userProfile?.let {
                                showDialog = true
                            }
                        }
                    }
                },
                enabled = isCorrect,
                modifier = Modifier.padding(bottom = 16.dp)

            ) {
                Text(text = stringResource(id = R.string.ready))
            }

            // Dialog to show when the exercise is completed correctly
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        onNextExercise()
                    },
                    title = { Text(text = stringResource(id = R.string.correct)) },
                    text = { Text(text = stringResource(id = R.string.points_earned, points)) },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            onNextExercise()
                        }) {
                            Text(text = stringResource(id = R.string.next_exercise))
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondExercise(
    onNextExercise: () -> Unit,
    onGoBack: () -> Unit,
    userProfileDao: UserProfileDao,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    // List of languages and corresponding countries
    val languageCountryPairs = remember {
        listOf(
            "Finnish" to "Finland",
            "Spanish" to "Spain",
            "French" to "France",
            "German" to "Germany",
        )
    }

    // State to track user input for the translations
    val userTranslations = remember {
        mutableStateOf(List(languageCountryPairs.size) { "" })
    }

    // State to control the visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }

    val points = POINTS_PER_EXERCISE

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.exercise_2)) },
            navigationIcon = {
                IconButton(onClick = { onGoBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.match_country),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display the language-country pairs
            languageCountryPairs.forEachIndexed { index, (language, _) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Display the language
                    Text(
                        text = language,
                        modifier = Modifier.weight(1f)
                    )

                    // Wrap the text field in a Box with weight modifier
                    Box(modifier = Modifier.width(200.dp)) {
                        OutlinedTextField(
                            value = userTranslations.value[index],
                            onValueChange = { newValue ->
                                val newList = userTranslations.value.toMutableList()
                                newList[index] = newValue
                                userTranslations.value = newList
                            },
                            label = { Text(text = stringResource(id = R.string.country_name)) },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            singleLine = true,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Check if user input matches the correct translations, ignoring case sensitivity
            val isCorrect =
                userTranslations.value.map { it.lowercase() } == languageCountryPairs.map { it.second.lowercase() }

            // Display feedback based on user input
            if (userTranslations.value.any { it.isNotEmpty() }) {
                Text(
                    text = if (isCorrect) stringResource(id = R.string.correct) else stringResource(
                        id = R.string.keep_trying
                    ),
                    color = if (isCorrect) Color.Black else Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue button (enabled only if the translations are correct)
            Button(
                onClick = {
                    if (isCorrect) {
                        userProfileViewModel.viewModelScope.launch {
                            val userProfile = updatePointsAndProceed(userProfileDao, points, userProfileViewModel)
                            userProfile?.let {
                                showDialog = true
                            }
                        }
                    }
                },
                enabled = isCorrect,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.ready))
            }
            // Dialog to show when the exercise is completed correctly
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        onNextExercise()
                    },
                    title = { Text(text = stringResource(id = R.string.correct)) },
                    text = { Text(text = stringResource(id = R.string.points_earned, points)) },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            onNextExercise()
                        }) {
                            Text(text = stringResource(id = R.string.next_exercise))
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiltExercise(
    sensorHelper: SensorHelper,
    onExerciseCompleted: () -> Unit,
    onGoBack: () -> Unit,
    userProfileDao: UserProfileDao,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    // Define the vocabulary pairs (English word, correct translation, wrong translation)
    val vocabulary = remember {
        listOf(
            Triple("Apple", "Manzana", "Naranja"),
            Triple("Banana", "Banana", "Uva"),
            Triple("Orange", "Naranja", "Manzana"),
            Triple("Grape", "Uva", "Banana"),
            Triple("Strawberry", "Fresa", "Tomate")
        )
    }

    // Track the current item being displayed
    var currentItemIndex by remember { mutableIntStateOf(0) }

    // Display the current item
    val currentItem = vocabulary[currentItemIndex]

    // Randomize the position of the correct and incorrect translations
    val isCorrectOnLeft = remember { mutableStateOf(Random.nextBoolean()) }

    // Feedback text
    var feedbackText by remember { mutableStateOf<String?>(null) }

    // Store the string resources in variables
    val correctString = stringResource(id = R.string.correct)
    val keepTryingString = stringResource(id = R.string.keep_trying)

    val points = POINTS_PER_EXERCISE

    // State to control the visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentItemIndex) {
        while (true) {
            delay(200) // Adjust delay as needed
            // Check if the device is tilted correctly
            if (sensorHelper.isTiltedRight.value) { // Check for right tilt instead of left
                if (isCorrectOnLeft.value) {
                    feedbackText = correctString // Provide feedback for correct tilt
                    Log.d(
                        "TiltExercise",
                        "Tilted left1, selected answer: ${currentItem.second}"
                    ) // Log the selected answer
                    delay(3000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                    if (currentItemIndex < vocabulary.size - 1) {
                        currentItemIndex++
                        isCorrectOnLeft.value = Random.nextBoolean()
                    } else {
                        userProfileViewModel.viewModelScope.launch {
                            val userProfile =
                                updatePointsAndProceed(userProfileDao, points, userProfileViewModel)
                            userProfile?.let {
                                showDialog = true
                            }
                        }

                        break
                    }
                } else {
                    feedbackText = keepTryingString // Provide feedback for incorrect tilt
                    Log.d(
                        "TiltExercise",
                        "Tilted right, selected answer: ${currentItem.third}"
                    ) // Log the selected answer
                    delay(2000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                }
            } else if (sensorHelper.isTiltedLeft.value) { // Check for left tilt instead of right
                if (!isCorrectOnLeft.value) {
                    feedbackText = correctString // Provide feedback for correct tilt
                    Log.d(
                        "TiltExercise",
                        "Tilted right3, selected answer: ${currentItem.second}"
                    ) // Log the selected answer
                    delay(3000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                    if (currentItemIndex < vocabulary.size - 1) {
                        currentItemIndex++
                        isCorrectOnLeft.value = Random.nextBoolean()
                    } else {
                        userProfileViewModel.viewModelScope.launch {
                            val userProfile =
                                updatePointsAndProceed(userProfileDao, points, userProfileViewModel)
                            userProfile?.let {
                                showDialog = true
                            }
                        }
                        break
                    }
                } else {
                    feedbackText = keepTryingString // Provide feedback for incorrect tilt
                    Log.d(
                        "TiltExercise",
                        "Tilted left, selected answer: ${currentItem.third}"
                    ) // Log the selected answer
                    delay(2000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                }
            }
        }
    }
    // Show the word and possible translations
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.exercise_3),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = { onGoBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                }
            }
        )
        Text(
            text = stringResource(id = R.string.exercise3_guide),

            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier, 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        val image =
            when (currentItem.first) {
                "Apple" -> R.drawable.apple
                "Banana" -> R.drawable.banana
                "Orange" -> R.drawable.orange
                "Grape" -> R.drawable.grape
                "Strawberry" -> R.drawable.strawberry
                else -> R.drawable.fruit
            }
        Image(
            painter = painterResource(id = image),
            contentDescription = "Fruits",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )

        // Display the word
        Text(
            text = currentItem.first, // Display the word
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp) // Increased padding
        )

        // Display the possible translations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isCorrectOnLeft.value) currentItem.second else currentItem.third, // Display the left translation
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp) // Increased padding
            )

            Text(
                text = if (isCorrectOnLeft.value) currentItem.third else currentItem.second, // Display the right translation
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp) // Increased padding
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val progressRight by animateFloatAsState(
                targetValue = if (sensorHelper.isTiltedRight.value) 1f else 0f,
                animationSpec = tween(durationMillis = 1000), label = ""
            )

            LinearProgressIndicator(
                progress = { progressRight },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .graphicsLayer(scaleX = -1f),
                color = Color.Green,
            )

            val progressLeft by animateFloatAsState(
                targetValue = if (sensorHelper.isTiltedLeft.value) 1f else 0f,
                animationSpec = tween(durationMillis = 1000), label = ""
            )

            LinearProgressIndicator(
                progress = { progressLeft },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = Color.Green,

                )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Show feedback text only when there is feedback
        feedbackText?.let {
            Text(
                text = it,
                color = if (it == stringResource(id = R.string.correct)) Color.Green else Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Dialog to show when the exercise is completed correctly
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onExerciseCompleted()
                },
                title = { Text(text = stringResource(id = R.string.correct)) },
                text = { Text(text = stringResource(id = R.string.points_earned, points)) },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        onExerciseCompleted()
                    }) {
                        Text(text = stringResource(id = R.string.next_exercise))
                    }
                }
            )
        }
    }
}


// Helper function to update points and move to the next exercise
suspend fun updatePointsAndProceed(
    userProfileDao: UserProfileDao,
    points: Int,
    viewModel: UserProfileViewModel
): UserProfile? {
    // Perform database operations within a coroutine
    val userProfile = withContext(Dispatchers.IO) {
        userProfileDao.getAllUserProfiles().firstOrNull()
    }

    userProfile?.let { profile ->
        val currentLanguage = profile.languages.find { it.name == profile.currentLanguage.name }
        currentLanguage?.let { language ->
            language.pointsEarned += points
            language.exercisesDone++ // Increment exercisesDone
            language.exerciseTimestamp = System.currentTimeMillis()
            profile.languagePoints = profile.languages.sumOf { it.pointsEarned }
            profile.exercisesDone = profile.exercisesDone?.plus(1) // Increment exercisesDone
            profile.pointsEarned += points // Increment pointsEarned
            withContext(Dispatchers.IO) {
                userProfileDao.updateUserProfile(profile)
            }
        }
    }
    return userProfile
}