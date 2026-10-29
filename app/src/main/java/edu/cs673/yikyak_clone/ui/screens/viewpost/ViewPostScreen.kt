package edu.cs673.yikyak_clone.ui.screens.viewpost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import edu.cs673.yikyak_clone.data.repository.SupabaseCommentRepository
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import kotlinx.datetime.Instant

@Composable
fun ViewPostScreen(postId: String) {
    val viewModel = remember { ViewPostViewModel(postId, SupabaseCommentRepository()) }
    val uiState = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                SocialPostCard(
                    user_id = "anonymous",
                    content = "Is the waffle machine at the dining hall working?",
                    latitude = 40.7357,
                    longitude = 74.1724,
                    score = 5,
                    comment_count = 0,
                    is_removed = false,
                    created_at = Instant.parse("2026-10-27T21:38:00Z"),
                    onLikeClick = { },
                )
            }
            items(threadComments(uiState.comments)) { threadedComment ->
                CommentRow(
                    comment = threadedComment.comment,
                    depth = threadedComment.depth,
                    onReplyClick = { viewModel.onReplyClick(threadedComment.comment) },
                )
            }
        }
        val parentComment = uiState.replyingTo
        if (parentComment != null) {
            Row {
                Text("Replying to ${parentComment.alias}")
                Button(onClick = { viewModel.onCancelReply() }) { Text("Cancel") }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = uiState.commentText,
                onValueChange = { newText -> viewModel.onCommentTextChange(newText) },
                label = { Text("Leave a comment!") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { viewModel.onSendClick() }) { Text("Send") }
        }
    }
}
