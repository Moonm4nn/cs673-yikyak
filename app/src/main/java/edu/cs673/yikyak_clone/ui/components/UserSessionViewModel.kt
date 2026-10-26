package edu.cs673.yikyak_clone.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.model.User
import edu.cs673.yikyak_clone.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserSessionViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profile =
        MutableStateFlow<User?>(null)

    val profile = _profile.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profile.value =
                userRepository.getUser(userId)
        }
    }
}