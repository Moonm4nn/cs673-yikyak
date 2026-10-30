package edu.cs673.yikyak_clone.ui.components

import edu.cs673.yikyak_clone.R
import edu.cs673.yikyak_clone.ui.theme.UpvoteGreen
import edu.cs673.yikyak_clone.ui.theme.DownvoteRed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
// import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant



@Composable
fun SocialPostCard(
    user_id: String,
    // avatarUrl: String,
    // postImageUrl: String?,
    content: String,
    latitude: Double,
    longitude: Double,
    score: Int,
    userVote: Int,
    comment_count: Int,
    is_removed: Boolean,
    created_at: Instant,
    onCommentsClick: () -> Unit = {},
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
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
                    text = user_id,
                    fontWeight = FontWeight.Bold
                )
            }

            /*if (!postImageUrl.isNullOrBlank()) {
                /*AsyncImage(
                    model = postImageUrl,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentScale = ContentScale.Crop
                )*/
            }*/

            Column(Modifier.padding(12.dp)) {
                Text(text = content)
                // Add vote controls and reply count here.
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                IconButton(onClick = onUpvote) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.arrow_shape_up
                        ),
                        contentDescription = "Up arrow",
                        tint = if (userVote == 1) UpvoteGreen
                        else LocalContentColor.current,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(text = "$score", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDownvote) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.arrow_shape_up
                        ),
                        contentDescription = "Down arrow",
                        tint = if (userVote == -1) DownvoteRed
                        else LocalContentColor.current,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(180f)
                    )
                }
                Button(onClick = onCommentsClick) {
                    Text(text = "$comment_count comments")
                }
            }
        }
    }
}