package com.example.languagelegends.aicomponents

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagelegends.R
import com.hexascribe.vertexai.VertexAI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AiChatViewModel : ViewModel() {
    var menuVisibility = MutableLiveData<Boolean>()
    var topic = MutableLiveData<String>()
    var response = MutableLiveData<String>()
    private val vertexAI by lazy {
        VertexAI.Builder()
            .setAccessToken("")
            .setProjectId("onyx-elevator-414111")
            .build()
    }

    private val textRequest by lazy {
        vertexAI.textRequest()
            .setModel("text-unicorn")
            .setTemperature(0.8)
            .setMaxTokens(256)
            .setTopK(40)
            .setTopP(0.8)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun executeRequest(text: String): String {
        val resultFlow = MutableStateFlow<String?>(null)

        viewModelScope.launch {
            val result = textRequest.execute(text).getOrThrow()
            resultFlow.value = result
            Log.d("DBG", "Result: $result")
        }

        return suspendCancellableCoroutine { continuation ->
            val collector = viewModelScope.launch {
                resultFlow.collect { result ->
                    if (result != null) {
                        continuation.resume(result)
                    }
                }
            }

            continuation.invokeOnCancellation {
                // Cancel the collector if coroutine is cancelled
                collector.cancel()
            }
        }
    }

    @Composable
    fun CardView(){
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
                            topic.value = "Cafe"
                            menuVisibility.value = false
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
                            topic.value = "Transport"
                            menuVisibility.value = false
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
                            topic.value = "Shopping"
                            menuVisibility.value = false
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
                            topic.value = "Weather"
                            menuVisibility.value = false
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
                            topic.value = "School"
                            menuVisibility.value = false
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
                            topic.value = "Health"
                            menuVisibility.value = false
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

    @Composable
    fun AiChat(topic: String, aiChatViewModel: AiChatViewModel) {
        var message: String by remember { mutableStateOf("") }
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "You chose $topic, the Wizard will now test your knowledge.",
                    textAlign = TextAlign.Center
                )
            }
            Row {
                Spacer(modifier = Modifier.width(40.dp))
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Enter your message") })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    viewModelScope.launch {
                        response.value = executeRequest(message)
                        message = ""
                    }
                }) {
                    Text("Send")
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Log.d("DBG", "Response: ${response.value}")
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = response.value.toString(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
