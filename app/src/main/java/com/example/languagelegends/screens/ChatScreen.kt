package com.example.languagelegends.screens

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.languagelegends.R
import com.example.languagelegends.aicomponents.AiChatViewModel

class ChatScreen {

    @Composable
    fun Chats() {

        val context = LocalContext.current
        val application = context.applicationContext as Application
        val viewModel = AiChatViewModel(application)

        val topic by viewModel.topic.observeAsState("")
        val menuVisibility by viewModel.menuVisibility.observeAsState(true)
        val response by viewModel.response.observeAsState("")


        // Display the chat screen
        Surface {
            if (menuVisibility) {
                CardView(viewModel)
            } else {
                AiChat(viewModel, topic, response, viewModel::onAskMeAQuestion, viewModel::checkAnswer)
            }
        }
    }
}


@Composable
fun AiChat(
    viewModel: AiChatViewModel,
    topic: String,
    response: String?,
    onAskMeAQuestion: () -> Unit,
    onCheckAnswer: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selectedLanguageName", "English (American)") ?: "English (American)"
    val isGeneratingQuestion by viewModel.isGeneratingQuestion.observeAsState(false)
    val resultMessage by viewModel.resultMessage.observeAsState("")
    val isQuestionAsked by viewModel.isQuestionAsked.observeAsState(false)


    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Display AI choice based on topic
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    id = R.string.ai_choice,
                    topic
                ),
                textAlign = TextAlign.Center
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            // Button to ask a question
            Button(onClick = {
                onAskMeAQuestion()
            }) {
                Text(text = stringResource(id = R.string.ask_question))
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            // Display AI response

            if (isGeneratingQuestion) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (!response.isNullOrEmpty()) "Translate this to $selectedLanguage: $response" else "",
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            // Text field for user's answer
            TextField(
                value = viewModel.userAnswer.value,
                onValueChange = { newValue ->
                    viewModel.userAnswer.value = newValue
                },
                label = { Text(stringResource(id = R.string.AIanswer)) },
                enabled = isQuestionAsked,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            // Button to check the answer
            Button(onClick = {
                onCheckAnswer()
            }) {
                Text(text = stringResource(id = R.string.check_answer))
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            resultMessage?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CardView(viewModel: AiChatViewModel) {
    // Resolve topic strings
    val coffeeTopic = stringResource(id = R.string.cafe)
    val transportTopic = stringResource(id = R.string.transport)
    val shoppingTopic = stringResource(id = R.string.shopping)
    val temperatureTopic = stringResource(id = R.string.weather)
    val schoolTopic = stringResource(id = R.string.school)
    val healthTopic = stringResource(id = R.string.health)

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Display AI wizard text
            Text(
                text = stringResource(id = R.string.ai_wizard),
                modifier = Modifier.fillMaxWidth(),
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            // Display choose topic text
            Text(
                text = stringResource(id = R.string.choose_topic),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.height(50.dp))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MakeCard(viewModel = viewModel, topic = coffeeTopic, iconId = R.drawable.coffee)
                Spacer(modifier = Modifier.width(10.dp))
                MakeCard(
                    viewModel = viewModel,
                    topic = transportTopic,
                    iconId = R.drawable.transport
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                MakeCard(viewModel = viewModel, topic = shoppingTopic, iconId = R.drawable.shopping)
                Spacer(modifier = Modifier.width(10.dp))
                MakeCard(
                    viewModel = viewModel,
                    topic = temperatureTopic,
                    iconId = R.drawable.temperature
                )

            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                MakeCard(viewModel = viewModel, topic = schoolTopic, iconId = R.drawable.school)
                Spacer(modifier = Modifier.width(10.dp))
                MakeCard(viewModel = viewModel, topic = healthTopic, iconId = R.drawable.health)

            }
        }
    }
}

@Composable

fun MakeCard(viewModel: AiChatViewModel, topic: String, iconId: Int) {

    Card(
        modifier = Modifier
            .size(150.dp)
            .padding(2.dp),
        onClick = {

            viewModel.topic.value = topic
            viewModel.menuVisibility.value = false
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                painter = painterResource(id = iconId),
                contentDescription = topic
            )

            Text(
                text = topic,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
