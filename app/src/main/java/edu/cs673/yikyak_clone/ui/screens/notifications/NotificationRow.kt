package edu.cs673.yikyak_clone.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.model.Notification

@Composable
fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    val target = if (notification.type == "post_reply") "your post" else "your comment"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
        ) {
            Text(
                text = "${notification.alias} replied to $target",
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = notification.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
