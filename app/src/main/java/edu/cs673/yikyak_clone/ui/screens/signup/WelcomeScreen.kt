package edu.cs673.yikyak_clone.ui.screens.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import androidx.compose.ui.Alignment
import edu.cs673.yikyak_clone.ui.components.ProfileImage
import edu.cs673.yikyak_clone.ui.screens.feed.FeedScreen
import androidx.compose.runtime.*

@Preview (showBackground = true)
@Composable
fun WelcomeScreen() {
    var showFeed by remember { mutableStateOf(false) }

    if (showFeed) {
        AccountCreationScreen()
    } else {
        Scaffold(

            bottomBar = {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                showFeed = true
                                println("Welcome to YikYak")
                            }
                        ) {
                            Text("Get connected")
                        }
                    }
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    12.dp,
                    Alignment.CenterVertically
                )
            ) {
                Text(
                    text = "Welcome to YIKYAK",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Connect With your Campus Community",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Background Visuals Coming soon"
                )
            }
        }
    }
}

