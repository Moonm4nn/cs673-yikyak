package edu.cs673.yikyak_clone.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.cs673.yikyak_clone.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                val destinations = listOf(
                    Screen.Feed,
                    Screen.Channels,
                    Screen.Notifications,
                    Screen.Profile
                )

                destinations.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(
                                    navController.graph
                                        .findStartDestination().id
                                ) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Text(screen.route.take(1).uppercase())
                        },
                        label = {
                            Text(
                                when (screen) {
                                    Screen.Feed -> "Feed"
                                    Screen.Channels -> "Channels"
                                    Screen.Notifications -> "Alerts"
                                    Screen.Profile -> "Profile"
                                    else -> screen.route
                                }
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.CreatePost.route)
                }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen()
            }

            composable(Screen.CreatePost.route) {
                CreatePostScreen()
            }

            composable(Screen.Channels.route) {
                ChannelsScreen()
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}