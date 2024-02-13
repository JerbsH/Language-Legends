package com.example.languagelegends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.languagelegends.R


@Composable
fun ExercisesScreen(selectedExercise: Int, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Exercise $selectedExercise",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Include Match Words Exercise
        if (selectedExercise == 1) {
            WordScrambleExercise(
                onNextExercise = { /* Handle navigation to the next exercise */ },
                onGoBack = { navController.navigate("path") } // Correct route here
            )
        } else {
            Text("No exercise found for $selectedExercise")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScrambleExercise(
    onNextExercise: () -> Unit,
    onGoBack: () -> Unit
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

    // Shuffle the letters of the current word
    val shuffledLetters = remember {
        currentWord.toCharArray().apply {
            shuffle()
        }
    }

    // State to track user input for the unscrambled word
    var userInput by remember { mutableStateOf("") }

    // State to track if the user's input is correct
    var isCorrect by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text(text = "Word Scramble Exercise") },
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
                text = "Unscramble the word:",
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
                label = { Text(text = "Your answer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Check if the user input matches the original word
            isCorrect = userInput.equals(currentWord, ignoreCase = true)

            // Display feedback based on user input
            if (userInput.isNotEmpty()) {
                Text(
                    text = if (isCorrect) "Correct!" else "Keep trying!",
                    color = if (isCorrect) Color.Green else Color.Red,
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
                Text(text = "Continue")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
