package com.example.languagelegends.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagelegends.R
import com.example.languagelegends.aicomponents.AiChatViewModel
import com.example.languagelegends.features.Message
import com.example.languagelegends.features.UserProfileViewModel

class ChatScreen {


    @Composable
    fun Chats(userProfileViewModel: UserProfileViewModel) {
        val context = LocalContext.current
        val application = context.applicationContext as Application
        val viewModel = AiChatViewModel(application, userProfileViewModel)

        val topic by viewModel.topic.observeAsState("")
        val menuVisibility by viewModel.menuVisibility.observeAsState(true)
        val response by viewModel.response.observeAsState("")
        var isFreeChat by remember { mutableStateOf(false) }


        // Display the chat screen
        Surface {
            if (isFreeChat) {
                FreeChatScreen(viewModel, viewModel::onFreeChat)
            } else {
                if (menuVisibility) {
                    CardView(viewModel) {
                        isFreeChat = true
                    }
                } else {
                    AiChat(
                        viewModel,
                        userProfileViewModel,
                        topic,
                        response,
                        viewModel::onAskMeAQuestion,
                        viewModel::checkAnswer,
                        viewModel::requestHint
                    )
                }
            }
        }
    }


    @Composable
    fun AiChat(
        viewModel: AiChatViewModel,
        userProfileViewModel: UserProfileViewModel,
        topic: String,
        response: String?,
        onAskMeAQuestion: () -> Unit,
        onCheckAnswer: () -> Unit,
        onRequestHint: () -> Unit
    ) {
        val questionLanguage by viewModel.questionLanguage.observeAsState(initial = "English")
        val isGeneratingQuestion by viewModel.isGeneratingQuestion.observeAsState(false)
        val resultMessage by viewModel.resultMessage.observeAsState("")
        val isQuestionAsked by viewModel.isQuestionAsked.observeAsState(false)
        val hintState by viewModel.hint.collectAsState()
        val keyboardController = LocalSoftwareKeyboardController.current




        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Display AI choice based on topic
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                text = stringResource(id = R.string.ai_choice, topic),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
            )

            // Button to ask a question
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = {
                    onAskMeAQuestion()
                }
            ) {
                Text(text = stringResource(id = R.string.ask_question))
            }

            // Display AI response or loading indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalConfiguration.current.screenHeightDp.dp * 1 / 8)
            ) {
                if (isGeneratingQuestion) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = if (!response.isNullOrEmpty()) "Translate this to $questionLanguage: $response" else "",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Text field for user's answer
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                value = viewModel.userAnswer.value,
                onValueChange = { newValue ->
                    viewModel.userAnswer.value = newValue
                },
                label = { Text(stringResource(id = R.string.AIanswer)) },
                enabled = isQuestionAsked,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onCheckAnswer()
                        keyboardController?.hide()
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onCheckAnswer()
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(id = R.string.AIchat_send)
                        )
                    }
                }
            )


            // Display result message
            resultMessage?.let {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    text = it,
                    textAlign = TextAlign.Center,
                )
            }

            // Button to request a hint
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = {
                    onRequestHint()
                }
            ) {
                Text(text = stringResource(id = R.string.request_hint))
            }

            // Display hint
            if (hintState.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    text = stringResource(id = R.string.request_hint) + ": " + hintState,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                )
            }

        }
    }
}

@Composable
fun FreeChatScreen(
    viewModel: AiChatViewModel,
    onFreeChat: (String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    val messages by viewModel.messages.observeAsState(emptyList())
    val isGeneratingAnswer by viewModel.isGeneratingQuestion.observeAsState(false)
    val keyboardController = LocalSoftwareKeyboardController.current
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = lazyListState
        ) {
            item {
                ChatMessage(message = Message(stringResource(id = R.string.AIwelcometext), false))
            }

            items(messages) { message ->
                ChatMessage(message = message)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Text field for user's answer
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                value = userInput,
                onValueChange = { newValue ->
                    userInput = newValue
                },
                label = { Text(stringResource(id = R.string.AItextfield)) },
                enabled = !isGeneratingAnswer,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onFreeChat(userInput)
                        userInput = ""
                        keyboardController?.hide()
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onFreeChat(userInput)
                            userInput = ""
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(id = R.string.AIchat_send)
                        )
                    }
                }
            )
        }

        if (isGeneratingAnswer) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        // Use LaunchedEffect to scroll to the last item when the LazyColumn recomposes
        LaunchedEffect(messages.size) {
            lazyListState.scrollToItem(messages.size)
        }
    }
}


@Composable
fun ChatMessage(message: Message) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}


@Composable
fun CardView(viewModel: AiChatViewModel, onFreeChatClicked: () -> Unit) {
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
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    viewModel.topic.value = ""
                    viewModel.menuVisibility.value = false
                    onFreeChatClicked()
                },
                modifier = Modifier.widthIn(150.dp) // Set a max width
            ) {
                Text(text = stringResource(id = R.string.free_chat))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
