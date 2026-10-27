package edu.cs673.yikyak_clone.ui.screens.viewpost

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.cs673.yikyak_clone.data.model.Comment

@Composable
fun CommentRow(comment: Comment, onReplyClick: () -> Unit) {
    Row {
        Text("${comment.content} by ${comment.alias}")
        Button(onClick = onReplyClick) { Text("Reply") }
    }
}

@Preview
@Composable
fun CommentRowPreview() {
    CommentRow(
        comment = Comment(
            id = "1",
            content = "Yes it works!",
            score = 2,
            createdAt = "10/24/26",
            alias = "GreenOtter",
            parentId = null,
        ),
        onReplyClick = { },
    )
}
