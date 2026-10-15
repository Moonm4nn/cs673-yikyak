package edu.cs673.yikyak_clone.ui.screens.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.ui.components.ProfileImage
import edu.cs673.yikyak_clone.ui.navigation.AppNavigation

// ProfileScreen.kt
@Preview (showBackground = true)
@Composable
fun AccountCreationScreen() {
    var username by remember { mutableStateOf(usernameGenerator()) }
    var isEditing by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf("") }
    var showFeed by remember { mutableStateOf(false) }

    if (showFeed) {
        AppNavigation()
    } else {
        Scaffold(
            bottomBar = {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showFeed = true }
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Anonymous Profile",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "No real name needed. Just be yourself."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        // Add settings functionality here
                    }) {
                        Text("Settings")
                    }
                }

                ProfileImage()

                if (isEditing) {
                    OutlinedTextField(
                        value = editedUsername,
                        onValueChange = { editedUsername = it },
                        label = { Text("Username") },
                        singleLine = true
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (editedUsername.isNotBlank()) {
                                    username = editedUsername.trim()
                                    isEditing = false
                                }
                            }
                        ) {
                            Text("Save")
                        }

                        OutlinedButton(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                editedUsername = username
                                isEditing = true
                            }
                        ) {
                            Text("Edit")
                        }

                        Button(
                            onClick = {
                                username = usernameGenerator()
                            }
                        ) {
                            Text("Generate")
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "3 Karma",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Your identity stays anonymous.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Simple Username Generator returns name like "TiredStudent-1000"
 *
 */
fun usernameGenerator(): String {
    val userAdjective = listOf(
        "Studious", "Annoying", "Tired", "Late"
    )

    val userNoun = listOf(
        "Student", "Engineer", "Researcher"
    )

    val adjective = userAdjective.random()
    val noun = userNoun.random()
    val userNum = (1000..9999).random()

    return "$adjective$noun-$userNum"
}