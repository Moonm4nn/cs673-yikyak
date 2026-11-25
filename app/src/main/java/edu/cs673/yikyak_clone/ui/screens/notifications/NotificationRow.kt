package edu.cs673.yikyak_clone.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import edu.cs673.yikyak_clone.data.model.Notification

@Composable
fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    val target = if (notification.type == "post_reply") "your post" else "your comment"
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            text = "${notification.alias} replied to $target",
            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
        )
        Text(text = notification.content)
    }
}
