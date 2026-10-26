package edu.cs673.yikyak_clone.data.repository

import android.net.Uri


/*
Repository layer for post operations (keeps Data separate from UI)
for creating / retrieving posts while keeping database details
out of the UI and ViewModel
*/


interface PostRepository {
    suspend fun createPost(
        postContent: String,
        imageUri: Uri? = null
    ): Result<Unit>
}

class SupabasePostRepository: PostRepository {
    override suspend fun createPost(
        postContent: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            //TODO: supabase call to create post
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}