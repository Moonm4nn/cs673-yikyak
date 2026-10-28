package edu.cs673.yikyak_clone.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.repository.SupabaseAuthRepository
import edu.cs673.yikyak_clone.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionBootViewModel(
    private val authRepository: SupabaseAuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow<SessionState>(SessionState.Loading)

    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val bootMutex = Mutex()

    init {
        boot()
    }

    fun boot() {
        viewModelScope.launch {
            bootMutex.withLock {
                _state.value = SessionState.Loading

                try {
                    val beforeId = authRepository.currentUserId()
                    android.util.Log.d("SessionBoot", "User before ensure: $beforeId")

                    val userId = authRepository.ensureAnonymousSession()
                    android.util.Log.d("SessionBoot", "User after ensure: $userId")

                    val user = userRepository.getUser(userId)

                    _state.value = when {
                        user == null ->
                            SessionState.NeedsOnboarding(userId)

                        !user.onboardingCompleted ||
                                user.username.isNullOrBlank() ->
                            SessionState.NeedsOnboarding(userId)

                        else ->
                            SessionState.Ready(userId)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _state.value = SessionState.Error(
                        e.message
                            ?: "Unable to load your session. Please retry."
                    )
                }
            }
        }
    }
}