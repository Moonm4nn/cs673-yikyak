package edu.cs673.yikyak_clone.ui.screens.viewpost

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.cs673.yikyak_clone.data.model.Comment
import edu.cs673.yikyak_clone.ui.components.SocialPostCard

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
    LazyColumn {
        item {
            SocialPostCard(
                username = "anonymous",
                avatarUrl = "",
                postImageUrl = "",
                caption = "Is the waffle machine at the dining hall working?",
                onLikeClick = { },
            )
        }
        items(mockComments) { comment ->
            CommentRow(comment = comment)
        }
    }
}

@Preview
@Composable
fun ViewPostScreenPreview() {
    ViewPostScreen()
}
