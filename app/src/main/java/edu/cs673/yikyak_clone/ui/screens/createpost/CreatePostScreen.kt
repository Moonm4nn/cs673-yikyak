package edu.cs673.yikyak_clone.ui.screens.createpost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import edu.cs673.yikyak_clone.data.getCurrentLocation
import edu.cs673.yikyak_clone.data.repository.SupabasePostRepository

private const val MAX_POST_LENGTH = 200

@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPostSuccess: () -> Unit = {}
) {
    val repository = remember { SupabasePostRepository() }
    val viewModel = remember { CreatePostViewModel(repository) }
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val postTypeExpanded = remember { mutableStateOf(false) }
    val postTypeOptions = listOf("Post", "Event")
    val postTypeSelectedOption = remember { mutableIntStateOf(0) }


    LaunchedEffect(uiState.postCreated) {
        if (uiState.postCreated) {
            onPostSuccess()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Text("←")
                    }
                    Box (modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                    ) {
                        // Button which creates dropdown menu
                        Button(onClick = { postTypeExpanded.value = true }) {
                            Text("New " + postTypeOptions[postTypeSelectedOption.intValue] + " ↓")
                        }
                        // Dropdown menu which responds to button click
                        DropdownMenu(
                            expanded = postTypeExpanded.value,
                            onDismissRequest = { postTypeExpanded.value = false }
                        ) {
                            // Create dropdown menu items for each option
                            postTypeOptions.forEachIndexed { index, option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        postTypeSelectedOption.intValue = index
                                        postTypeExpanded.value = false
                                    }
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            getCurrentLocation(context) { lat, lon ->
                                viewModel.onUiAction(CreatePostUiAction.UpdateLocation(lat, lon))
                                viewModel.onUiAction(CreatePostUiAction.CreatePost())
                            }
                        },
                            enabled = uiState.postContent.isNotBlank() &&
                                    uiState.postContent.length <= MAX_POST_LENGTH &&
                                    !uiState.isLoading,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.postContent,
                onValueChange = { newText ->
                    if (newText.length <= MAX_POST_LENGTH) {
                        viewModel.onUiAction(
                            CreatePostUiAction.UpdatePostContent(newText)
                        )
                    }
                },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            // Only appears if post type is event
            if (postTypeSelectedOption.intValue == 1) {
                Text("Date/Time1")
                Text("Date/Time2")
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "${uiState.postContent.length} / $MAX_POST_LENGTH",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.postContent.length >= MAX_POST_LENGTH) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    CreatePostScreen(
        onPostSuccess = {},
        onBackClick = {}
    )
}

