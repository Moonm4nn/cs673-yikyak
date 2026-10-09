package edu.cs673.yikyak_clone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            12.dp,
            Alignment.CenterVertically
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Coming soon"
        )
    }
}

@Composable
fun FeedScreen() {
    PlaceholderScreen(
        title = "Feed",
        description = "Nearby posts will appear here."
    )
}

@Composable
fun CreatePostScreen() {
    PlaceholderScreen(
        title = "Create Post",
        description = "Compose an anonymous post here."
    )
}

@Composable
fun ChannelsScreen() {
    PlaceholderScreen(
        title = "Channels",
        description = "Explore topic-based communities."
    )
}

@Composable
fun NotificationsScreen() {
    PlaceholderScreen(
        title = "Notifications",
        description = "Your replies and notifications will appear here."
    )
}

@Composable
fun ProfileScreen() {
    PlaceholderScreen(
        title = "Profile",
        description = "Your profile and account details will appear here."
    )
}

@Composable
fun SettingsScreen() {
    PlaceholderScreen(
        title = "Settings",
        description = "Manage account and privacy preferences."
    )
}