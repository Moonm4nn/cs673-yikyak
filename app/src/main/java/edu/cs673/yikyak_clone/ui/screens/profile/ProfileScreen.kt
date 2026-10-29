package edu.cs673.yikyak_clone.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.ui.components.GeotaggedPostScreen
import edu.cs673.yikyak_clone.ui.components.ProfileImage
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import edu.cs673.yikyak_clone.ui.components.UserSessionViewModel


// ProfileScreen.kt
@Composable

fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: UserSessionViewModel
) {
    val profile by viewModel.profile
        .collectAsStateWithLifecycle()

    val samplePost = SocialPost(
        id = "post-1",
        username = "anonymous",
        avatarUrl = "",
        postImageUrl = "",
        caption = "Anyone know what's happening on campus today?",
        score = 0,
        userVote = 0
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.Right
        )
        {
            Button( onClick = onNavigateToSettings) {
                Text(text = "Settings")
            }
        }
        ProfileImage()

        Text(text = profile?.username ?: "Loading...")
        Text(text = "${profile?.karma ?: "0"}")
        Text(text = "Karma")
        GeotaggedPostScreen()

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

