package com.example.languagelegends.screens

import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.languagelegends.aicomponents.AiChatViewModel

class ChatScreen {
    private val viewModel: AiChatViewModel = AiChatViewModel()

    @Composable
    fun Chats() {
        val topic by viewModel.topic.observeAsState("")
        val menuVisibility by viewModel.menuVisibility.observeAsState(true)
        val response by viewModel.response.observeAsState("")

        Surface {
            if (menuVisibility) {
                CardView(viewModel)
            } else {
                AiChat(topic, response) {
                    viewModel.onAskMeAQuestion()
                }
            }
        }
    }
}

@Composable
fun AiChat(topic: String, response: String?, onAskMeAQuestion: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "You chose $topic, the Wizard will now test your knowledge.",
                textAlign = TextAlign.Center
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                onAskMeAQuestion()
            }) {
                Text("Ask Me a Question")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Log.d("DBG", "Response: $response")
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = response.toString(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CardView(viewModel: AiChatViewModel) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "AI chat Wizard",
                modifier = Modifier.fillMaxWidth(),
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Choose a topic",
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
                MakeCard(viewModel = viewModel, topic = "Coffee")
                Spacer(modifier = Modifier.width(10.dp))
                MakeCard(viewModel = viewModel, topic = "Transport")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MakeCard(viewModel = viewModel, topic = "Shopping")
                Spacer(modifier = Modifier.width(10.dp))
                MakeCard(viewModel = viewModel, topic = "Temperature")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MakeCard(viewModel = viewModel, topic = "School")
                Spacer(modifier = Modifier.width(10.dp))
                MakeCard(viewModel = viewModel, topic = "Health")
            }
        }
    }
}

@Composable
fun MakeCard(viewModel: AiChatViewModel, topic: String){
    val iconId = when (topic) {
        "Coffee" -> com.example.languagelegends.R.drawable.coffee
        "Transport" -> com.example.languagelegends.R.drawable.transport
        "Shopping" -> com.example.languagelegends.R.drawable.shopping
        "Temperature" -> com.example.languagelegends.R.drawable.temperature
        "School" -> com.example.languagelegends.R.drawable.school
        "Health" -> com.example.languagelegends.R.drawable.health
        else -> com.example.languagelegends.R.drawable.coffee
    }
    Card(
        modifier = Modifier
            .size(150.dp)
            .padding(2.dp),
        onClick = {
            viewModel.topic.value =  topic
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