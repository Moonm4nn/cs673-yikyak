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

    override suspend fun createPost(
        postContent: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri?
    ): Result<Unit> {
        createCallCount += 1
        lastPostContent = postContent
        lastImageUri = imageUri
        return result
    }

    override suspend fun getPosts(
        pageNum: Int,
        pageSize: Int,
        latitude: Double,
        longitude: Double,
        sortByHot: Boolean
    ): List<SocialPost> {
        createCallCount += 1
        return posts
    }

    override suspend fun getPost(postId: String): Result<SocialPost> {
        TODO("Not yet implemented")
    }
}