package edu.cs673.yikyak_clone.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import edu.cs673.yikyak_clone.R
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

// ProfileScreen.kt
@Preview (showBackground = true)
@Composable
fun ProfileScreen() {
    val samplePost = SocialPost(
        id = "post-1",
        username = "anonymous",
        avatarUrl = "",
        postImageUrl = "",
        caption = "Anyone know what's happening on campus today?"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Row(

        )
        {
            Button(
                onClick = {
                    println("Button clicked!")
                }
            ) {
                Text(text = "Edit")
            }
            Button(
                onClick = {
                    println("Button clicked!")
                }
            ) {
                Text(text = "Settings")
            }
        }
        Image (
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "formatted profile image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )

        Text(text = "Username")
        Text(text = "3")
        Text(text = "Karma")

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
}

data class SocialPost(
    val id: String,
    val username: String,
    val avatarUrl: String,
    val postImageUrl: String,
    val caption: String
)
