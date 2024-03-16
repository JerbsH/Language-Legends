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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.languagelegends.OnCompleteExercise
import com.example.languagelegends.aicomponents.AiChatViewModel
import com.example.languagelegends.features.UserProfileViewModel

/**This function displays the path screen. It shows a list of exercises that the user can navigate to.
 *The exercises are represented as circles on a path.
 *The circles are clickable if the exercise is unlocked.
 *The path is scrollable and the background image is loaded from a resource.
 */
@Composable
fun PathScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
    apiSelectedLanguage: String,
    aiChatViewModel: AiChatViewModel,
    totalCompletedExercises: Int,
    onCompleteExercise: OnCompleteExercise,
    viewState: ViewState
) {
    aiChatViewModel.chatVisible.value = false
    val deviceHeight = LocalConfiguration.current.screenHeightDp.dp * 3
    val scrollState = rememberScrollState(initial = deviceHeight.value.toInt())

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
                completedExercises = viewState.getCompletedExercises(),
            ) {
                // Navigate to the ExercisesScreen when exercise is clicked
                number ->
                viewState.setCurrentLevel(number)
                navController.navigate("exercises/${number}")
            }
        }
    }
}

/** This list contains the positions of the exercises on the path.
 *The positions are represented as pairs of floats,
 *where the first float is the x-coordinate and the second float is the y-coordinate.
 */
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

/** This function displays a single exercise on the path.
 *The exercise is represented as a circle.
 *The circle is clickable if the exercise is unlocked.
 *The color of the circle depends on whether the exercise is unlocked or not.
 */
@Composable
fun LanguageExercise(
    number: Int,
    x: Float,
    y: Float,
    completedExercises: Int,
    onClick: (Int) -> Unit,
) {
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
            onClick = { onClick(number) }
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

