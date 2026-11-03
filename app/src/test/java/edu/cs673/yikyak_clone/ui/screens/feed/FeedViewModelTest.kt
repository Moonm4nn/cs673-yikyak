package edu.cs673.yikyak_clone.ui.screens.feed
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.data.repository.FakePostRepository
import edu.cs673.yikyak_clone.data.repository.FakeVoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.After
import org.junit.Before
import kotlin.String

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testPost(
        id: String = "post-1",
        content: String = "Test post",
        score: Int = 5,
        userVote: Int = 0,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
    ) = SocialPost(
        id = id,
        user_id = "user-1",
        content = content,
        latitude = latitude,
        longitude = longitude,
        userVote = userVote,
        score = score,
        comment_count = 0,
        is_removed = false,
        created_at = Instant.parse("2026-06-01T12:00:00Z")
    )

    private fun createLoadedViewModel(
        post: SocialPost,
        voteRepository: FakeVoteRepository
    ): FeedViewModel {
        val postRepository = FakePostRepository(posts = listOf(post))
        val viewModel = FeedViewModel(postRepository, voteRepository)
        viewModel.onUiAction(FeedUiAction.UpdateLocation(37.421, -122.084))
        assertEquals(1, viewModel.uiState.posts.size)
        return viewModel
    }

    // --- Voting ---
    @Test
    fun `upvote updates score and vote and calls repository`() {
        val voteRepository = FakeVoteRepository()
        val viewModel = createLoadedViewModel(
            testPost(id = "post-1", score = 5, userVote = 0),
            voteRepository
        )

        viewModel.onVote("post-1", 1)

        val result = viewModel.uiState.posts.first { it.id == "post-1" }
        assertEquals(1, result.userVote)
        assertEquals(6, result.score)
        assertEquals(1, voteRepository.callCount)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun `same arrow twice removes vote`() {
        val voteRepository = FakeVoteRepository()
        val viewModel = createLoadedViewModel(
            testPost(id = "post-1", score = 5, userVote = 0),
            voteRepository
        )

        viewModel.onVote("post-1", 1)
        viewModel.onVote("post-1", 1)

        val result = viewModel.uiState.posts.first { it.id == "post-1" }
        assertEquals(0, result.userVote)
        assertEquals(5, result.score)
        assertEquals(2, voteRepository.callCount)
    }

    @Test
    fun `vote repository failure rolls back and shows error`() {
        val voteRepository = FakeVoteRepository(
            result = Result.failure(Exception("Network error"))
        )
        val viewModel = createLoadedViewModel(
            testPost(id = "post-1", score = 5, userVote = 0),
            voteRepository
        )
        viewModel.onVote("post-1", 1)

        val result = viewModel.uiState.posts.first { it.id == "post-1" }

        assertEquals(0, result.userVote)
        assertEquals(5, result.score)
        assertNotNull(viewModel.uiState.errorMessage)
        assertEquals(1, voteRepository.callCount)
    }

    @Test
    fun `post removed from feed when downvote threshold reached`() {
        val voteRepository = FakeVoteRepository()
        val viewModel = createLoadedViewModel(
            testPost(id = "post-1", score = -2, userVote = 0),
            voteRepository
        )

        viewModel.onVote("post-1", -1)
        assertTrue(viewModel.uiState.posts.none { it.id == "post-1" })
        assertEquals(1, voteRepository.callCount)
    }
}
