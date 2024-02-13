package com.example.languagelegends.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.languagelegends.R

class ChatScreen {
    @Composable
    fun Chats() {
        var menuVisibility by remember { mutableStateOf(true) }
        var topic = ""
        Surface {
            if (menuVisibility) {
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
                                    topic = "Cafe"
                                    menuVisibility = false
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
                                    topic = "Transport"
                                    menuVisibility = false
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
                                    topic = "Shopping"
                                    menuVisibility = false
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
                                    topic = "Weather"
                                    menuVisibility = false
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
                                    topic = "School"
                                    menuVisibility = false
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
                                    topic = "Health"
                                    menuVisibility = false
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

            } else {
                AiChat(topic)
            }

        }
    }

    @Composable
    fun AiChat(topic: String) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "You chose $topic, the Wizard will now test your knowledge.",
                textAlign = TextAlign.Center
            )
        }
    }
}
