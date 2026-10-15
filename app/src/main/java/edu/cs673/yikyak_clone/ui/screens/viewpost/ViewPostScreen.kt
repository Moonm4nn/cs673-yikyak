package edu.cs673.yikyak_clone.ui.screens.viewpost

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.cs673.yikyak_clone.ui.components.SocialPostCard

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
    }
}

@Preview
@Composable
fun ViewPostScreenPreview() {
    ViewPostScreen()
}
