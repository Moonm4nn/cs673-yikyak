package edu.cs673.yikyak_clone.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import edu.cs673.yikyak_clone.data.repository.SupabaseAuthRepository
import edu.cs673.yikyak_clone.data.repository.SupabaseUserRepository
import edu.cs673.yikyak_clone.ui.components.UserSessionViewModel
import edu.cs673.yikyak_clone.ui.screens.ChannelsScreen
import edu.cs673.yikyak_clone.ui.screens.NotificationsScreen
import edu.cs673.yikyak_clone.ui.screens.createpost.CreatePostScreen
import edu.cs673.yikyak_clone.ui.screens.editprofile.EditProfileScreen
import edu.cs673.yikyak_clone.ui.screens.feed.FeedScreen
import edu.cs673.yikyak_clone.ui.screens.profile.ProfileScreen
import edu.cs673.yikyak_clone.ui.screens.settings.SettingsScreen
import edu.cs673.yikyak_clone.ui.screens.signup.AccountCreationScreen
import edu.cs673.yikyak_clone.ui.screens.signup.AccountCreationViewModel
import edu.cs673.yikyak_clone.ui.screens.signup.WelcomeScreen
import edu.cs673.yikyak_clone.ui.screens.viewpost.ViewPostScreen
import edu.cs673.yikyak_clone.ui.session.SessionBootViewModel
import edu.cs673.yikyak_clone.ui.session.SessionState
import edu.cs673.yikyak_clone.R

/** Screens where the bottom navbar and + floating icon are expected*/
val navbarScreens = listOf(
    Screen.Profile.route,
    Screen.Channels.route,
    Screen.Notifications.route,
    Screen.Profile.route,
    Screen.Feed.route
)

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    @DrawableRes val iconRes: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Feed, "Feed", R.drawable.home),
    BottomNavItem(Screen.Channels, "Explore", R.drawable.explore_compass),
    BottomNavItem(Screen.Notifications, "Alerts", R.drawable.notification_bell),
    BottomNavItem(Screen.Profile, "Profile", R.drawable.profile)
)

@Preview(showBackground = true)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val userRepository = remember {
        SupabaseUserRepository(SupabaseClientProvider.client)
    }

    val authRepository = remember {
        SupabaseAuthRepository(SupabaseClientProvider.client)
    }

    val accountCreationViewModel: AccountCreationViewModel =
        viewModel(
            factory = remember(userRepository) {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        if (modelClass.isAssignableFrom(
                                AccountCreationViewModel::class.java
                            )
                        ) {
                            @Suppress("UNCHECKED_CAST")
                            return AccountCreationViewModel(
                                userRepository,
                                authRepository
                            ) as T
                        }

                        throw IllegalArgumentException(
                            "Unknown ViewModel class: ${modelClass.name}"
                        )
                    }
                }
            }
        )

    val userSessionViewModel: UserSessionViewModel =
        viewModel(
            factory = remember(userRepository) {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        if (
                            modelClass.isAssignableFrom(
                                UserSessionViewModel::class.java
                            )
                        ) {
                            @Suppress("UNCHECKED_CAST")
                            return UserSessionViewModel(
                                userRepository
                            ) as T
                        }

                        throw IllegalArgumentException(
                            "Unknown ViewModel class: ${modelClass.name}"
                        )
                    }
                }
            }
        )

    val sessionBootViewModelFactory = remember(
        authRepository,
        userRepository
    ) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass.isAssignableFrom(
                        SessionBootViewModel::class.java
                    )
                ) {
                    @Suppress("UNCHECKED_CAST")
                    return SessionBootViewModel(
                        authRepository,
                        userRepository
                    ) as T
                }

                throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }
    }

    val sessionBootViewModel: SessionBootViewModel = viewModel(
        factory = sessionBootViewModelFactory
    )

    val sessionState by
    sessionBootViewModel.state.collectAsState()

    LaunchedEffect(
        accountCreationViewModel.createdUserId
    ) {
        if (accountCreationViewModel.createdUserId != null) {
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.AccountCreation.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in navbarScreens) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(
                                        navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.SessionBoot.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen(
                    onPostClick = { postId -> navController.navigate(Screen.ViewPost.createRoute(postId)) },
                    onCreatePostClick = { channelId, channelName ->
                        navController.navigate(Screen.CreatePost.createRoute(channelId, channelName))
                    },
                    onSearchChannelsClick = {
                        navController.navigate(Screen.Channels.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.ViewPost.route) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")
                if (postId != null) {
                    ViewPostScreen(
                        postId,
                        onBackClick = {navController.popBackStack()},
                    )
                }
            }

            composable(
                    route = Screen.CreatePost.route,
                arguments = listOf(
                    navArgument("channelId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("channelName") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                CreatePostScreen(
                    channelId = entry.arguments?.getString("channelId"),
                    channelName = entry.arguments?.getString("channelName"),
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPostSuccess = {navController.popBackStack()}
                )
            }

            composable(Screen.Channels.route) {
                ChannelsScreen()
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen()
            }

            composable(Screen.Profile.route) {
                val currentUserId = authRepository.currentUserId()

                LaunchedEffect(currentUserId) {
                    currentUserId?.let { id ->
                        userSessionViewModel.loadProfile(id)
                    }
                }

                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    viewModel = userSessionViewModel
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetConnected = {
                        navController.navigate(Screen.AccountCreation.route)
                    }
                )
            }

            composable(Screen.AccountCreation.route) {
                AccountCreationScreen(
                    onCreateUser = { username ->
                        accountCreationViewModel.completeOnboarding(
                            username = username
                        )
                    },
                    isLoading = accountCreationViewModel.isLoading,
                    creationError = accountCreationViewModel.errorMessage
                )
            }

            composable(Screen.SessionBoot.route) {
                when (val state = sessionState) {
                    SessionState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is SessionState.NeedsOnboarding -> {
                        LaunchedEffect(state.userId) {
                            navController.navigate(
                                Screen.Welcome.route
                            ) {
                                popUpTo(Screen.SessionBoot.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }

                    is SessionState.Ready -> {
                        LaunchedEffect(state.userId) {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(Screen.SessionBoot.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }

                    is SessionState.Error -> {
                        androidx.compose.foundation.layout.Column {
                            Text(state.message)
                            Button(
                                onClick = {
                                    sessionBootViewModel.boot()
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}