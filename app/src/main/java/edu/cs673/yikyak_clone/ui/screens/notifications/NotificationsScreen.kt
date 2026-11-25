package edu.cs673.yikyak_clone.ui.screens.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.repository.SupabaseNotificationRepository

@Composable
fun NotificationsScreen(onNotificationClick: (String) -> Unit) {
    val viewModel = remember {
        NotificationsViewModel(
            SupabaseNotificationRepository(),
        )
    }
    val uiState = viewModel.uiState

    Column {
        Text(
            text = "Alerts",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> Text(text = uiState.errorMessage)
            uiState.notifications.isEmpty() -> Text(text = "No alerts yet")
            else -> {
                LazyColumn {
                    items(uiState.notifications) { notification ->
                        NotificationRow(
                            notification,
                            onClick = { onNotificationClick(notification.postId) },
                        )
                    }
                }
            }
        }
    }
}
