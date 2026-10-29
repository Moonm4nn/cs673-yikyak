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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.cs673.yikyak_clone.data.model.Comment
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import kotlinx.datetime.Instant
import java.util.UUID

val mockComments = listOf(
    Comment(
        id = "1",
        content = "Yes it works!",
        score = 2,
        createdAt = "10/24/26",
        alias = "GreenOtter",
        parentId = null,
    ),
    Comment(
        id = "2",
        content = "are you sure?",
        score = 1,
        createdAt = "10/24/26",
        alias = "PurpleFish",
        parentId = "1",
    ),
    Comment(
        id = "3",
        content = "yeah, I saw it too",
        score = 2,
        createdAt = "10/24/26",
        alias = "OrangeCat",
        parentId = "2",
    ),
)

@Composable
fun ViewPostScreen() {
    var comments by remember { mutableStateOf(mockComments) }
    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                SocialPostCard(
                    user_id = "anonymous",
                    caption = "Is the waffle machine at the dining hall working?",
                    latitude = 40.7357,
                    longitude = 74.1724,
                    score = 5,
                    comment_count = 0,
                    is_removed = false,
                    created_at = Instant.parse("2026-10-27T21:38:00Z"),
                    onLikeClick = { },
                )
            }
            items(threadComments(comments)) { threadedComment ->
                CommentRow(
                    comment = threadedComment.comment,
                    depth = threadedComment.depth,
                    onReplyClick = { replyingTo = threadedComment.comment },
                )
            }
        }
        val parentComment = replyingTo
        if (parentComment != null) {
            Row {
                Text("Replying to ${parentComment.alias}")
                Button(onClick = { replyingTo = null }) { Text("Cancel") }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = commentText,
                onValueChange = { newText -> commentText = newText },
                label = { Text("Leave a comment!") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (commentText.isBlank()) return@Button

                val newComment = Comment(
                    id = UUID.randomUUID().toString(),
                    content = commentText.trim(),
                    score = 0,
                    createdAt = "10/26/26",
                    alias = "Anon-1234",
                    parentId = replyingTo?.id,
                )

                comments = comments + newComment

                replyingTo = null
                commentText = ""
            }) { Text("Send") }
        }
    }
}

@Preview
@Composable
fun ViewPostScreenPreview() {
    ViewPostScreen()
}
