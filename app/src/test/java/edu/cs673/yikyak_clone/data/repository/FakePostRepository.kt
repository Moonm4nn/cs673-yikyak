package edu.cs673.yikyak_clone.data.repository

import android.net.Uri

class FakePostRepository(
    private var result: Result<Unit> = Result.success(Unit),
    var createCallCount: Int = 0,
    var lastPostContent: String? = null
) : PostRepository {
    override suspend fun createPost(
        postContent: String,
        imageUri: Uri?
    ): Result<Unit> {
        createCallCount += 1
        lastPostContent = postContent
        return result
    }
}
