package edu.cs673.yikyak_clone.ui.session

sealed interface SessionState {
    data object Loading : SessionState

    data class NeedsOnboarding(
        val userId: String
    ) : SessionState

    data class Ready(
        val userId: String
    ) : SessionState

    data class Error(
        val message: String
    ) : SessionState
}