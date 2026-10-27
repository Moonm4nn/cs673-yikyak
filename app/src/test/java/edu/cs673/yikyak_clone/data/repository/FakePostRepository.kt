package edu.cs673.yikyak_clone.data.repository

import android.net.Uri

class FakePostRepository(
    private var result: Result<Unit> = Result.success(Unit)
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
}