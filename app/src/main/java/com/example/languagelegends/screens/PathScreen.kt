package com.example.languagelegends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable

fun PathScreen(navController: NavController, apiSelectedLanguage: String) {
    val completedExercises by remember { mutableIntStateOf(0) }
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

    Pair(0.16f, 1.41f),//ball 1
    Pair(0.25f, 1.253f),
    Pair(0.187f, 1.121f),
    Pair(0.243f, 1.01f),
    Pair(0.205f, 0.90f),
    Pair(0.155f, 0.692f),
    Pair(0.255f, 0.511f),
    Pair(0.19f, 0.383f),
    Pair(0.242f, 0.215f),
    Pair(0.21f, 0.08f),// Ball 10
)

@Composable
fun LanguageExercise(
    modifier: Modifier = Modifier,
    number: Int,
    x: Float,
    y: Float,
    completedExercises: Int,
    onClick: (Int) -> Unit,
) {
    // Determine if the exercise is unlocked
    val isUnlocked = number <= completedExercises + 1

    // Determine the circle color based on the unlocked status
    val circleColor = if (isUnlocked) Color(0xFF573C1A) else Color(0xFF996B2F)

    // Calculate the positions as a percentage of the screen width and height
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val aspectRatio = 20.0f / 9.0f // Replace with the aspect ratio of your design
    val adjustedScreenWidth = screenHeight * aspectRatio
    val offsetX = (x * adjustedScreenWidth) - (screenWidth / 2)
    val offsetY = y * screenHeight

    // Render the circle as a clickable surface if the exercise is unlocked
    if (isUnlocked) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .offset(offsetX.dp, offsetY.dp),
            shape = CircleShape,
            color = circleColor,
            onClick = { onClick(number) } // Call the lambda onClick with the exercise number
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
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
                .offset(offsetX.dp, offsetY.dp),
            shape = CircleShape,
            color = circleColor,
            onClick = {}
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
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
