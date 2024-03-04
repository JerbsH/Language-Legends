package com.example.languagelegends.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PathScreen(navController : NavController, apiSelectedLanguage: String) {
    var completedExercises by remember { mutableStateOf(0) }

    // Load the background image
    val backgroundImage = painterResource(id = com.example.languagelegends.R.drawable.path)

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = backgroundImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Create a loop for generating numbers 1-10
            for (i in 10 downTo 1) { // Start from 10 and go down to 1
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Add spacers on the sides
                    Spacer(modifier = Modifier.weight(1f))
                    // Render each number
                    LanguageExercise(
                        number = i,
                        completedExercises = completedExercises
                    ) {
                        // Navigate to the ExercisesScreen when exercise is clicked
                        navController.navigate("exercises/${it}")
                    }
                    // Add spacers on the sides
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
fun LanguageExercise(
    number: Int,
    completedExercises: Int,
    onClick: (Int) -> Unit

) {
    // Determine if the exercise is unlocked
    val isUnlocked = number <= completedExercises + 1

    // Determine the circle color based on the unlocked status
    val circleColor = if (isUnlocked) Color.Cyan else Color.Gray

    // Render the circle as a clickable surface if the exercise is unlocked
    if (isUnlocked) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = circleColor,
            onClick = { onClick(number) } // Call the lambda onClick with the exercise number
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    } else {
        // Render the circle as a non-clickable surface if the exercise is locked
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = circleColor,
            onClick = {}
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}