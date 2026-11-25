package edu.cs673.yikyak_clone.ui.screens.notifications

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import edu.cs673.yikyak_clone.data.repository.SupabaseNotificationRepository

@Composable
fun NotificationsScreen(onNotificationClick: (String) -> Unit) {
    val viewModel = remember {
        NotificationsViewModel(
            SupabaseNotificationRepository(),
        )
    }
    val uiState = viewModel.uiState

    when {
        uiState.isLoading -> CircularProgressIndicator()
        uiState.errorMessage != null -> Text(text = uiState.errorMessage)
        uiState.notifications.isEmpty() -> Text(text = "No notifications yet")
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
