package edu.cs673.yikyak_clone.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import androidx.compose.ui.Alignment
import edu.cs673.yikyak_clone.ui.components.ProfileImage
import edu.cs673.yikyak_clone.ui.components.ProfileList
import edu.cs673.yikyak_clone.ui.components.GeotaggedPostScreen
//import edu.cs673.yikyak_clone.ui.components.GeotaggedPostScreenOld

// ProfileScreen.kt
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
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
            Button( onClick = onNavigateToEditProfile) {
                Text(text = "Edit")
            }
            Button( onClick = onNavigateToSettings) {
                Text(text = "Settings")
            }
        }
        ProfileImage()

        Text(text = "Username")
        ProfileList()
        Text(text = "3")
        Text(text = "Karma")
        GeotaggedPostScreen()
        //GeotaggedPostScreenOld()

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
