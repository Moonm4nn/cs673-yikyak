package edu.cs673.yikyak_clone.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
// import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SocialPostCard(
    username: String,
    avatarUrl: String,
    postImageUrl: String?,
    caption: String,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keep your existing avatar and username UI here.
                Text(
                    text = username,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!postImageUrl.isNullOrBlank()) {
                /*AsyncImage(
                    model = postImageUrl,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentScale = ContentScale.Crop
                )*/
            }

            Column(Modifier.padding(12.dp)) {
                Text(text = caption)

                // Add vote controls and reply count here.
            }
        }
    }
}