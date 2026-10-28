package edu.cs673.yikyak_clone.ui.screens.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.model.User
import edu.cs673.yikyak_clone.data.repository.SupabaseAuthRepository
import edu.cs673.yikyak_clone.data.repository.UserRepository
import kotlinx.coroutines.launch

class AccountCreationViewModel(
    private val userRepository: UserRepository,
    private val authRepository: SupabaseAuthRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var user by mutableStateOf<User?>(null)
        private set

    var createdUserId by mutableStateOf<String?>(null)
        private set

    fun completeOnboarding(username: String) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            createdUserId = null

            try {
                //check for userID or make one
                val userId = authRepository.currentUserId()
                    ?: authRepository.signInAnonymously()

                val result = userRepository.completeOnboarding(
                    userId = userId,
                    username = username.trim()
                )

                result.fold(
                    onSuccess = {
                        val createdUser = userRepository.getUser(userId)

                        if (createdUser == null) {
                            errorMessage =
                                "User was created but could not be retrieved"
                        } else {
                            user = createdUser
                            createdUserId = userId
                        }
                    },
                    onFailure = { error ->
                        errorMessage = error.message
                            ?: "Could not create your profile"
                    }
                )
            } catch (error: Exception) {
                errorMessage = error.message
                    ?: "Something went wrong while creating your profile"
            } finally {
                isLoading = false
            }
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                user = userRepository.getUser(userId)

                if (user == null) {
                    errorMessage = "User not found"
                }
            } catch (error: Exception) {
                errorMessage = error.message
                    ?: "Could not retrieve your profile"
            } finally {
                isLoading = false
            }
        }
    }
}