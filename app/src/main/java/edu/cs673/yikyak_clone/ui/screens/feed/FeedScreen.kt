package edu.cs673.yikyak_clone.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import kotlinx.datetime.Instant
import kotlin.Double


// FeedScreen.kt
@Composable
fun FeedScreen() {
    val samplePost = SocialPost(
        id = "post-1",
        user_id = "anonymous",
        content = "Anyone know what's happening on campus today?",
        latitude = 40.7357,
        longitude = 74.1724,
        score = 5,
        userVote = 0,
        comment_count = 0,
        is_removed = false,
        created_at = Instant.parse("2026-10-27T21:38:00Z")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        item(key = samplePost.id) {
            SocialPostCard(
                user_id = samplePost.user_id,
                //avatarUrl = samplePost.avatarUrl,
                //postImageUrl = samplePost.postImageUrl,
                caption = samplePost.content,
                latitude = samplePost.latitude,
                longitude = samplePost.longitude,
                score = samplePost.score,
                //userVote = samplePost.userVote,
                comment_count = samplePost.comment_count,
                is_removed = samplePost.is_removed,
                created_at = samplePost.created_at,
                onLikeClick = {
                    // Add voting behavior later
                }
            )
        }
    }
}
