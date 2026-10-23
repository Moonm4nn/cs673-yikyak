package edu.cs673.yikyak_clone.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.ui.components.SocialPostCard


// FeedScreen.kt
@Composable
fun FeedScreen() {
    val samplePost = SocialPost(
        id = "post-1",
        username = "anonymous",
        avatarUrl = "",
        postImageUrl = "",
        caption = "Anyone know what's happening on campus today?"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        item(key = samplePost.id) {
            SocialPostCard(
                username = samplePost.username,
                avatarUrl = samplePost.avatarUrl,
                postImageUrl = samplePost.postImageUrl,
                caption = samplePost.caption,
                onLikeClick = {
                    // Add voting behavior later
                }
            )
        }
    }
}
