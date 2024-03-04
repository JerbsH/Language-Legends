package com.example.languagelegends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController


@Composable
fun PathScreen(navController: NavController) {
    val completedExercises by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    // Load the background image
    val backgroundImage = painterResource(id = com.example.languagelegends.R.drawable.path)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Image(
            painter = backgroundImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )

        exercisePositions.forEachIndexed { index, (x, y) ->
            LanguageExercise(
                number = index + 1,
                x = x,
                y = y,
                completedExercises = completedExercises,
            ) {
                // Navigate to the ExercisesScreen when exercise is clicked
                navController.navigate("exercises/${it}")
            }
        }
    }

    /*
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
                    //Spacer(modifier = Modifier.weight(1f))
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
    }*/
}

val exercisePositions = listOf(
    Pair(100.dp, 100.dp), // 1
    Pair(150.dp, 100.dp),
    Pair(250.dp, 150.dp),
    Pair(350.dp, 200.dp),
    Pair(450.dp, 250.dp),
    Pair(50.dp, 250.dp),
    Pair(150.dp, 200.dp),
    Pair(250.dp, 150.dp),
    Pair(350.dp, 100.dp),
    Pair(100.dp, 1000.dp) // 10
)

@Composable
fun LanguageExercise(
    modifier: Modifier = Modifier,
    number: Int,
    x: Dp,
    y: Dp,
    completedExercises: Int,
    onClick: (Int) -> Unit,
) {
    // Determine if the exercise is unlocked
    val isUnlocked = number <= completedExercises + 1

    // Determine the circle color based on the unlocked status
    val circleColor = if (isUnlocked) Color.Cyan else Color.Gray

    // Render the circle as a clickable surface if the exercise is unlocked
    if (isUnlocked) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .offset(x, y),
            shape = CircleShape,
            color = circleColor,
            onClick = { onClick(number) } // Call the lambda onClick with the exercise number
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
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
            modifier = Modifier
                .size(40.dp)
                .offset(x, y),
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