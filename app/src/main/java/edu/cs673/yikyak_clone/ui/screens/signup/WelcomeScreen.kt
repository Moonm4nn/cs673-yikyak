package edu.cs673.yikyak_clone.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun WelcomeScreen(
    onGetConnected: () -> Unit
) {
        Scaffold{ innerPadding ->

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

                Button(onClick = onGetConnected) {
                    Text("Get Connected")
                }
            }
        }
    }

