package edu.cs673.yikyak_clone.ui.screens.feed

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.model.SocialPost
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import edu.cs673.yikyak_clone.data.repository.SupabasePostRepository
import edu.cs673.yikyak_clone.ui.components.SocialPostCard

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier
) {
    val repository = remember { SupabasePostRepository() }
    val viewModel = remember { FeedViewModel(repository) }
    val uiState = viewModel.uiState
    val listState = rememberLazyListState()

    // Trigger initial load when screen enters composition
    LaunchedEffect(Unit) {
        viewModel.onUiAction(FeedUiAction.LoadInitialPosts)
    }

    // Monitor list scrolling position to load subsequent database chunks
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                // If the last visible item is close to the end of the list, load more data
                if (lastIndex != null && lastIndex >= uiState.posts.size - 3) {
                    viewModel.onUiAction(FeedUiAction.LoadNextPage)
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding()
    )
    {
        // Display Loading Indicator while posts are being loaded
        if (uiState.isLoading && uiState.posts.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        // Display Error Message if it exists
        else if (!uiState.errorMessage.isNullOrBlank() && uiState.posts.isEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        // Display Feed of Posts if everything is ready
        else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            )
            {
                // Display each Social Post identified by their ID
                items(uiState.posts, key = { it.id }) { post ->
                    SocialPostItem(post = post)
                }

                // Loading indicator displayed at the bottom of the list during lazy pagination
                if (uiState.isLoading && uiState.posts.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

// Social Post Item
@Composable
fun SocialPostItem(post: SocialPost) {
    SocialPostCard(
        user_id = post.user_id,
        content = post.content,
        latitude = post.latitude,
        longitude = post.longitude,
        score = post.score,
        comment_count = post.comment_count,
        is_removed = post.is_removed,
        created_at = post.created_at,
        onLikeClick = {/* Add voting behavior later*/}
    )
}