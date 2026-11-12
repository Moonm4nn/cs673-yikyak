package edu.cs673.yikyak_clone.ui.screens.feed

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cs673.yikyak_clone.data.model.SocialPost
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import edu.cs673.yikyak_clone.data.repository.SupabasePostRepository
import edu.cs673.yikyak_clone.data.repository.SupabaseVoteRepository
import edu.cs673.yikyak_clone.ui.components.SocialPostCard
import androidx.compose.ui.platform.LocalContext
import edu.cs673.yikyak_clone.data.getCurrentLocation
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import edu.cs673.yikyak_clone.R
import edu.cs673.yikyak_clone.data.repository.SupabaseChannelRepository
import edu.cs673.yikyak_clone.ui.components.ChannelImage
import edu.cs673.yikyak_clone.ui.screens.channel.ChannelListViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onPostClick: (String) -> Unit,
    onSearchChannelsClick: () -> Unit = {},
    onCreatePostClick: (channelId: String?, channelName: String?) -> Unit = { _, _ -> }
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // null = public (nearby) feed, otherwise the id of the selected channel
    var selectedChannelId by rememberSaveable { mutableStateOf<String?>(null) }

    val channelListViewModel = remember { ChannelListViewModel(SupabaseChannelRepository()) }
    val channelState = channelListViewModel.uiState
    val channels = channelState.channels
    val selectedChannel = channels.firstOrNull { it.id == selectedChannelId }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = "Channels",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onSearchChannelsClick()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.magnifying_glass),
                            contentDescription = "Search channels",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Entry for the public feed
                NavigationDrawerItem(
                    label = { Text("Nearby") },
                    icon = { ChannelImage("Nearby") },
                    selected = selectedChannelId == null,
                    onClick = {
                        selectedChannelId = null
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                when {
                    channelState.isLoading && channels.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.padding(28.dp))
                    }
                    channelState.errorMessage != null && channels.isEmpty() -> {
                        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)) {
                            Text(channelState.errorMessage, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { channelListViewModel.loadChannels() }) { Text("Retry") }
                        }
                    }
                }

                channels.forEach { channel ->
                    NavigationDrawerItem(
                        label = { Text(channel.name) },
                        icon = { ChannelImage(channel.name) },
                        selected = selectedChannelId == channel.id,
                        onClick = {
                            selectedChannelId = channel.id
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.channel_menu
                            ),
                            contentDescription = "Open Channels"
                        )
                    }
                    Text(
                        text = selectedChannel?.name ?: "Yik Yak",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Changing the key rebuilds everything inside: a new ViewModel, scroll position, and location fetch
                key(selectedChannelId) {
                    FeedContent(
                        channelId = selectedChannelId,
                        onPostClick = onPostClick
                    )
                }
            }
            FloatingActionButton(
                onClick = { onCreatePostClick(selectedChannelId, selectedChannel?.name) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("+")
            }
        }

    }
}

@Composable
private fun FeedContent(
    channelId: String?,
    onPostClick: (String) -> Unit
) {
    val postRepository = remember { SupabasePostRepository() }
    val voteRepository = remember { SupabaseVoteRepository() }
    val viewModel = remember { FeedViewModel(postRepository, voteRepository, channelId) }
    val uiState = viewModel.uiState
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Get user's location and trigger initial load when feed opens
    LaunchedEffect(Unit) {
        getCurrentLocation(context) { lat, lng ->
            viewModel.onUiAction(
                FeedUiAction.UpdateLocation(lat, lng)
            )
        }
    }

    // Monitor list scrolling position to load subsequent database chunks
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                // If the last visible item is close to the end of the list, load more data
                if (lastIndex != null && lastIndex >= uiState.posts.size - 3) {
                    viewModel.onUiAction(FeedUiAction.LoadNextPage)
                }
            }
    }
    Column(
        modifier = Modifier.fillMaxSize()
            .fillMaxSize()
            .padding()
    )
    {
        Button(
            onClick = {
                // Toggle between hot and new posts
                viewModel.onUiAction(
                    FeedUiAction.SetSortByHot(
                        isHot = !uiState.sortByHot
                    )
                )
            },
            // Disable button if loading or refreshing
            enabled = !uiState.isLoading && !uiState.isRefreshing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                // Alternate text between hot and new
                text = if (uiState.sortByHot) "Hot" else "New"
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        )
        {
            // Display Loading Indicator while posts are being loaded
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Display Error Message if it exists
            else if (!uiState.errorMessage.isNullOrBlank() && uiState.posts.isEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            // Display Feed of Posts if everything is ready
            else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                )
                {
                    // Display each Social Post identified by their ID
                    items(uiState.posts, key = { it.id }) { post ->
                        SocialPostItem(
                            post = post,
                            onCommentsClick = { onPostClick(post.id) },
                            onUpvote = { viewModel.onVote(post.id, 1) },
                            onDownvote = { viewModel.onVote(post.id, -1) }
                        )
                    }

                    // Loading indicator displayed at the bottom of the list during lazy pagination
                    if (uiState.isLoading && uiState.posts.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}


// Social Post Item
@Composable
fun SocialPostItem(
    post: SocialPost,
    onCommentsClick: () -> Unit,
    onUpvote: () -> Unit = {},
    onDownvote: () -> Unit = {}
    ) {
    SocialPostCard(
        user_id = post.user_id,
        content = post.content,
        latitude = post.latitude,
        longitude = post.longitude,
        score = post.score,
        userVote = post.userVote,
        comment_count = post.comment_count,
        is_removed = post.is_removed,
        created_at = post.created_at,
        onCommentsClick = onCommentsClick,
        onUpvote = onUpvote,
        onDownvote = onDownvote
    )
}