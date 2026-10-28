package edu.cs673.yikyak_clone.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.cs673.yikyak_clone.ui.screens.*
import edu.cs673.yikyak_clone.ui.screens.feed.*
import edu.cs673.yikyak_clone.ui.screens.profile.*
import edu.cs673.yikyak_clone.ui.screens.createpost.CreatePostScreen
import edu.cs673.yikyak_clone.ui.screens.editprofile.*
import edu.cs673.yikyak_clone.ui.screens.settings.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider

import edu.cs673.yikyak_clone.data.repository.SupabaseAuthRepository
import edu.cs673.yikyak_clone.data.repository.SupabaseUserRepository
import edu.cs673.yikyak_clone.ui.components.UserSessionViewModel
import edu.cs673.yikyak_clone.ui.screens.signup.AccountCreationViewModel
import edu.cs673.yikyak_clone.ui.screens.signup.AccountCreationScreen
import edu.cs673.yikyak_clone.ui.screens.signup.WelcomeScreen
import edu.cs673.yikyak_clone.ui.session.SessionBootViewModel
import edu.cs673.yikyak_clone.ui.session.SessionState

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
            if (currentRoute != Screen.Welcome.route && currentRoute != Screen.AccountCreation.route) {
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
            }
        },
        floatingActionButton = {
            if (currentRoute != Screen.Welcome.route && currentRoute != Screen.AccountCreation.route) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.CreatePost.route)
                    }
                ) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.SessionBoot.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen()
            }

            composable(Screen.CreatePost.route) {
                CreatePostScreen(
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
                        CircularProgressIndicator()
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