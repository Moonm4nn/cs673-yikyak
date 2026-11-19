package edu.cs673.yikyak_clone.ui.screens.createpost

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import edu.cs673.yikyak_clone.data.getCurrentLocation
import edu.cs673.yikyak_clone.data.repository.SupabasePostRepository
import edu.cs673.yikyak_clone.ui.components.ChannelImage
import edu.cs673.yikyak_clone.R

private const val MAX_POST_LENGTH = 200

@Composable
fun CreatePostScreen(
    channelId: String? = null,
    channelName: String? = null,
    onBackClick: () -> Unit = {},
    onPostSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { SupabasePostRepository() }
    val viewModel = remember (channelId) { CreatePostViewModel(repository, channelId) }
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val displayName = if (channelId == null) "Nearby" else (channelName ?: "Channel")
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onUiAction(CreatePostUiAction.SetImage(uri))
    }


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
                )
                {
                    IconButton(onClick = onBackClick) {
                        Text("←")
                    }
                    Text(
                        text = "New Post",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    ChannelImage(displayName)
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
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
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.post_image_icon),
                            contentDescription = "Attach image",
                            modifier = Modifier.size(24.dp)
                        )
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

            uiState.imageUri?.let { uri ->
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    IconButton(
                        onClick = { viewModel.onUiAction(CreatePostUiAction.RemoveImage) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("✕", color = Color.White)
                    }
                }
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

