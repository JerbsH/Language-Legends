package com.example.languagelegends.screens

import android.app.Application
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.languagelegends.R
import com.example.languagelegends.aicomponents.AiChatViewModel

class ChatScreen {
    //private val viewModel: AiChatViewModel = AiChatViewModel()

    @Composable
    fun Chats() {

        val context = LocalContext.current
        val application = context.applicationContext as Application
        val viewModel = AiChatViewModel(application)

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
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(2.dp),
                    onClick = {
                        viewModel.topic.value = "Cafe"
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
                            painter = painterResource(id = R.drawable.baseline_coffee_24),
                            contentDescription = "coffee"
                        )
                        Text(
                            text = "Cafe",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                }
                Spacer(modifier = Modifier.width(10.dp))
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(2.dp),
                    onClick = {
                        viewModel.topic.value = "Transport"
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
                            painter = painterResource(id = R.drawable.baseline_directions_bus_24),
                            contentDescription = "bus"
                        )
                        Text(
                            text = "Transport",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(2.dp),
                    onClick = {
                        viewModel.topic.value = "Shopping"
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
                            painter = painterResource(id = R.drawable.baseline_shopping_cart_24),
                            contentDescription = "shopping cart"
                        )
                        Text(
                            text = "Shopping",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                }
                Spacer(modifier = Modifier.width(10.dp))
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(2.dp),
                    onClick = {
                        viewModel.topic.value = "Weather"
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
                            painter = painterResource(id = R.drawable.baseline_thermostat_24),
                            contentDescription = "thermostat"
                        )
                        Text(
                            text = "Weather",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(2.dp),
                    onClick = {
                        viewModel.topic.value = "School"
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
                            painter = painterResource(id = R.drawable.baseline_school_24),
                            contentDescription = "school"
                        )
                        Text(
                            text = "School",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                }
                Spacer(modifier = Modifier.width(10.dp))
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(2.dp),
                    onClick = {
                        viewModel.topic.value = "Health"
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
                            painter = painterResource(id = R.drawable.baseline_health_and_safety_24),
                            contentDescription = "Health and safety"
                        )
                        Text(
                            text = "Health",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
        }
    }
}