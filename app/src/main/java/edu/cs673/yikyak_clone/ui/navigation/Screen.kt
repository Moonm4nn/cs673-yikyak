package edu.cs673.yikyak_clone.ui.navigation

import android.net.Uri

/** data class of screen containing routes2
 *
 */
sealed class Screen(val route: String) {
    data object Feed : Screen("feed")
    data object CreatePost : Screen("createPost?channelId={channelId}&channelName={channelName}") {
        fun createRoute(channelId: String?, channelName: String?) =
            if (channelId == null) {"createPost"}
            else {
                "createPost?channelId=${Uri.encode(channelId)}&channelName=${Uri.encode(channelName ?: "")}"
            }
    }
    data object Channels : Screen("channels")
    data object Notifications : Screen("notifications")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object EditProfile : Screen("edit_profile")
    data object Welcome : Screen("welcome")
    data object AccountCreation : Screen("account_create")

    data object SessionBoot : Screen("session_boot")

    data object ViewPost : Screen("view_post/{postId}") {
        fun createRoute(postId: String) = "view_post/$postId"
    }
}