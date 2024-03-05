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
}

val exercisePositions = listOf(
    Pair(80.dp, 1165.dp), // Ball 1
    Pair(285.dp, 1048.dp),
    Pair(117.dp, 936.dp),
    Pair(263.dp, 850.dp),
    Pair(173.dp, 748.dp),
    Pair(70.dp, 570.dp),
    Pair(293.dp, 437.dp),
    Pair(130.dp, 320.dp),
    Pair(250.dp, 180.dp),
    Pair(186.dp, 65.dp) // Ball 10
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
    val circleColor = if (isUnlocked) Color(0xFF573C1A) else Color(0xFF996B2F)

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
                    style = MaterialTheme.typography.bodyLarge,
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}