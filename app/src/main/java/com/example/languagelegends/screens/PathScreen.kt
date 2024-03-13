package com.example.languagelegends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.languagelegends.OnCompleteExercise
import com.example.languagelegends.aicomponents.AiChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun PathScreen(navController: NavController, apiSelectedLanguage: String, aiChatViewModel: AiChatViewModel, totalCompletedExercises: Int, onCompleteExercise: OnCompleteExercise) {
    aiChatViewModel.chatVisible.value = false
    val deviceHeight = LocalConfiguration.current.screenHeightDp.dp * 3
    val scrollState = rememberScrollState(initial = deviceHeight.value.toInt())

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
        PointCounter(
            pointCount = totalCompletedExercises,
            modifier = Modifier.align(Alignment.TopStart)
        )

        exercisePositions.forEachIndexed { index, (x, y) ->
            LanguageExercise(
                number = index + 1,
                x = x,
                y = y,
                completedExercises = totalCompletedExercises,
            ) {
                // Navigate to the ExercisesScreen when exercise is clicked
                navController.navigate("exercises/${it}")
            }
        }
    }
}
    val exercisePositions = listOf(

        Pair(0.22f, 2.84f),//ball 1
        Pair(0.63f, 2.545f),
        Pair(0.34f, 2.26f),
        Pair(0.57f, 2.038f),
        Pair(0.41f, 1.82f),
        Pair(0.19f, 1.38f),
        Pair(0.64f, 1.05f),
        Pair(0.35f, 0.77f),
        Pair(0.55f, 0.444f),
        Pair(0.444f, 0.15f),// Ball 10
    )

    @Composable
    fun LanguageExercise(
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

        // Calculate the positions as a percentage of the smallest width
        val smallestWidth =
            minOf(
                LocalConfiguration.current.screenWidthDp,
                LocalConfiguration.current.screenHeightDp
            )
        val offsetX = x * smallestWidth
        val offsetY = y * smallestWidth

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

    @Composable
    fun PointCounter(pointCount: Int, modifier: Modifier = Modifier) {

        Column(
            modifier = modifier
                .padding(16.dp)
                .background(color = Color.Black.copy(alpha = 0.5f))
        ) {
            Text(
                text = "Points: $pointCount",
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
