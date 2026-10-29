package edu.cs673.yikyak_clone.ui.navigation


/** data class of screen containing routes
 *
 */
sealed class Screen(val route: String) {
    data object Feed : Screen("feed")
    data object CreatePost : Screen("create_post")
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