package edu.cs673.yikyak_clone.ui.screens.viewpost

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.model.Comment

private const val MAX_INDENT_DEPTH = 4
private const val INDENT_PER_LEVEL_DP = 16

@Composable
fun CommentRow(comment: Comment, depth: Int, onReplyClick: () -> Unit) {
    val indent = minOf(depth, MAX_INDENT_DEPTH) * INDENT_PER_LEVEL_DP
    Row(modifier = Modifier.padding(start = indent.dp)) {
        Text(text = "${comment.content} by ${comment.alias}")
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
        depth = 0,
    )
}
