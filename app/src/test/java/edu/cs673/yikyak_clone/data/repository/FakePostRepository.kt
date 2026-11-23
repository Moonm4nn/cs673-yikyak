package edu.cs673.yikyak_clone.data.repository

import android.net.Uri
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.data.repository.PostRepository


class FakePostRepository(
    private var result: Result<Unit> = Result.success(Unit),
    private var posts: List<SocialPost> = emptyList()
) : PostRepository {
    var createCallCount: Int = 0
    var lastPostContent: String? = null
    var lastImageUri: Uri? = null
    var lastLatitude: Double? = null
    var lastLongitude: Double? = null
    var getPostsCallCount: Int = 0
    var lastPageNum: Int? = null
    var lastPageSize: Int? = null
    var lastFeedLatitude: Double? = null
    var lastFeedLongitude: Double? = null
    var lastSortByHot: Boolean? = null
    var lastChannelId: String? = null
    var lastGetPostsChannelId: String? = null

    override suspend fun createPost(
        postContent: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri?,
        channelId: String?
    ): Result<Unit> {
        createCallCount += 1
        lastPostContent = postContent
        lastImageUri = imageUri
        lastLatitude = latitude
        lastLongitude = longitude
        lastChannelId = channelId
        return result
    }

    override suspend fun getPosts(
        pageNum: Int,
        pageSize: Int,
        latitude: Double,
        longitude: Double,
        sortByHot: Boolean,
        channelId: String?
    ): List<SocialPost> {
        getPostsCallCount++
        lastPageNum = pageNum
        lastPageSize = pageSize
        lastFeedLatitude = latitude
        lastFeedLongitude = longitude
        lastSortByHot = sortByHot
        lastGetPostsChannelId = channelId

        return posts
    }

    var getPostCallCount: Int = 0

    override suspend fun getPost(postId: String): Result<SocialPost> {
        getPostCallCount++
        val post = posts.firstOrNull { it.id == postId }
        return if (post != null) {
            Result.success(post)
        } else {
            Result.failure(NoSuchElementException("Post $postId not found"))
        }
    }
}