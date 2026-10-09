package edu.cs673.yikyak_clone.ui.navigation

sealed class Screen(val route: String) {
    data object Feed : Screen("feed")
    data object CreatePost : Screen("create_post")
    data object Channels : Screen("channels")
    data object Notifications : Screen("notifications")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}