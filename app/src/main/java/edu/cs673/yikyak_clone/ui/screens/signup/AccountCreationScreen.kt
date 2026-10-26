
package edu.cs673.yikyak_clone.ui.screens.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.ui.components.ProfileImage

@Composable
fun AccountCreationScreen(
    onCreateUser: (String) -> Unit,
    isLoading: Boolean,
    creationError: String?
) {
    var username by rememberSaveable {
        mutableStateOf(usernameGenerator())
    }

    var editedUsername by rememberSaveable {
        mutableStateOf("")
    }

    var isEditing by rememberSaveable {
        mutableStateOf(false)
    }

    var errorMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = {
                        val finalUsername = username.trim()

                        if (finalUsername.isNotBlank()) {
                            onCreateUser(finalUsername)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(52.dp),
                    enabled = username.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating profile...")
                    } else {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Create your identity",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Create your account to join the conversation.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileImage()

                    Text(
                        text = "YOUR ANONYMOUS USERNAME",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isEditing) {
                        OutlinedTextField(
                            value = editedUsername,
                            onValueChange = {
                                editedUsername = it
                                errorMessage = null
                            },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorMessage != null,
                            supportingText = {
                                errorMessage?.let { Text(it) }
                            }
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    editedUsername = ""
                                    errorMessage = null
                                }
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    val candidate = editedUsername.trim()

                                    when {
                                        candidate.isBlank() -> {
                                            errorMessage =
                                                "Username cannot be empty"
                                        }

                                        candidate.length > 30 -> {
                                            errorMessage =
                                                "Use 30 characters or fewer"
                                        }

                                        else -> {
                                            username = candidate
                                            isEditing = false
                                            errorMessage = null
                                        }
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    } else {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
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

                            TextButton(
                                onClick = {
                                    username = usernameGenerator()
                                }
                            ) {
                                Text("Regenerate")
                            }
                        }
                    }
                }
            }

            Text(
                text = "Your username is how others recognize you. " +
                        "You can change it here before continuing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stay anonymous. Be respectful. Build your community.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            creationError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
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
    val adjectives = listOf(
        "Studious", "Curious", "Tired", "Late", "Quiet", "Cosmic"
    )

    val nouns = listOf(
        "Student", "Engineer", "Researcher", "Thinker", "Owl"
    )

    return "${adjectives.random()}${nouns.random()}-${(1000..9999).random()}"
}
