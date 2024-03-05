package com.example.languagelegends.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.languagelegends.R
import com.example.languagelegends.features.SensorHelper
import kotlinx.coroutines.delay
import kotlin.random.Random


@Composable
fun ExercisesScreen(navController: NavController) {
    //Define the current exercise
    var currentExercise by remember { mutableStateOf(1) }

    //Define the layout for the exercises
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
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
                )
            }

            2 -> {
                SecondExercise(
                    onNextExercise = {
                        currentExercise++
                    },
                    onGoBack = { navController.navigate("path") }
                )
            }

            3 -> {
                TiltExercise(
                    sensorHelper = SensorHelper(LocalContext.current),
                    onExerciseCompleted = {
                        currentExercise++
                    }
                )
            }
            // Add more exercises as needed
            else -> {
                Text(
                    text = stringResource(id = R.string.all_exercises),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScrambleExercise(
    onNextExercise: () -> Unit,
    onGoBack: () -> Unit,
    sensorHelper: SensorHelper // Pass the sensor helper instance
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

    // Randomly select a word from the list
    val currentWord = remember {
        wordList.random()
    }

    var shuffledLetters by remember {
        mutableStateOf(
            currentWord.toCharArray().toList().shuffled()
        )
    }


    // Register shake detection when the composable is launched
    LaunchedEffect(Unit) {
        sensorHelper.setShakeListener {
            // Shuffle the letters when the device is shaken
            shuffledLetters = currentWord.toCharArray().toList().shuffled()
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

            // Display the picture of fruits
            Image(
                painter = painterResource(id = R.drawable.fruit),
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
                        onNextExercise()
                    }
                },
                enabled = isCorrect,
                modifier = Modifier.padding(bottom = 16.dp)

            ) {
                Text(text = stringResource(id = R.string.ready))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondExercise(
    onNextExercise: () -> Unit,
    onGoBack: () -> Unit
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
                        onNextExercise()
                    }
                },
                enabled = isCorrect,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.ready))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TiltExercise(

    sensorHelper: SensorHelper,
    onExerciseCompleted: () -> Unit
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
    var currentItemIndex by remember { mutableStateOf(0) }

    // Display the current item
    val currentItem = vocabulary[currentItemIndex]

    // Randomize the position of the correct and incorrect translations
    val isCorrectOnLeft = remember { mutableStateOf(Random.nextBoolean()) }

    // Feedback text
    var feedbackText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentItemIndex) {
        while (true) {
            delay(100) // Adjust delay as needed

            // Check if the device is tilted correctly
            if (sensorHelper.isTiltedLeft.value) {
                if (isCorrectOnLeft.value) {
                    feedbackText = "Correct!" // Provide feedback for correct tilt
                    delay(2000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                    if (currentItemIndex < vocabulary.size - 1) {
                        currentItemIndex++
                        isCorrectOnLeft.value = Random.nextBoolean()
                    } else {
                        onExerciseCompleted()
                        break
                    }
                } else {
                    feedbackText = "Wrong! Try again." // Provide feedback for incorrect tilt
                    delay(2000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                }
            } else if (sensorHelper.isTiltedRight.value) {
                if (!isCorrectOnLeft.value) {
                    feedbackText = "Correct!" // Provide feedback for correct tilt
                    delay(2000) // Wait for a second before clearing the feedback
                    feedbackText = null // Clear the feedback
                    if (currentItemIndex < vocabulary.size - 1) {
                        currentItemIndex++
                        isCorrectOnLeft.value = Random.nextBoolean()
                    } else {
                        onExerciseCompleted()
                        break
                    }
                } else {
                    feedbackText = "Wrong! Try again." // Provide feedback for incorrect tilt
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
        // Display the word
        Text(
            text = currentItem.first, // Display the word
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp) // Increased padding
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

        // Show feedback text only when there is feedback
        feedbackText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}