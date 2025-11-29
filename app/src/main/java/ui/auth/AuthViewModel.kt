package com.example.storeit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.storeit.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AuthMode { SignIn, Register }

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val mode: AuthMode = AuthMode.SignIn,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: FirebaseUser? = null,
    val userRole: String? = null,
    val inventoryId: String? = null,
    val inventoryName: String? = null
)

class AuthViewModel(private val auth: FirebaseAuth, private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(currentUser = auth.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        auth.currentUser?.let { refreshCurrentUser(it) }
    }

    private fun updateState(transform: (AuthUiState) -> AuthUiState) {
        _uiState.value = transform(_uiState.value)
    }

    fun onEmailChanged(value: String) = updateState { it.copy(email = value, errorMessage = null) }

    fun onPasswordChanged(value: String) = updateState { it.copy(password = value, errorMessage = null) }

    fun onConfirmPasswordChanged(value: String) = updateState { it.copy(confirmPassword = value, errorMessage = null) }

    fun toggleMode() = updateState {
        it.copy(
            mode = if (it.mode == AuthMode.SignIn) AuthMode.Register else AuthMode.SignIn,
            confirmPassword = "",
            errorMessage = null
        )
    }

    private fun refreshCurrentUser(user: FirebaseUser?) {
        // When refreshing user, clear the inventory selection to force a choice.
        updateState { it.copy(currentUser = user, inventoryId = null, userRole = null, inventoryName = null, isLoading = false, errorMessage = null) }
    }

    fun selectInventory(inventoryId: String, inventoryName: String, role: String) {
        updateState { it.copy(inventoryId = inventoryId, inventoryName = inventoryName, userRole = role) }
    }

    fun clearInventorySelection() {
        updateState { it.copy(inventoryId = null, userRole = null, inventoryName = null) }
    }

    fun submitCredentials() {
        val state = uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            updateState { it.copy(errorMessage = "Email and password are required.") }
            return
        }

        if (state.mode == AuthMode.Register && state.password != state.confirmPassword) {
            updateState { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = if (state.mode == AuthMode.SignIn) {
                    auth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                } else {
                    auth.createUserWithEmailAndPassword(state.email.trim(), state.password).await().also {
                        userRepository.createUser(it.user!!.uid)
                    }
                }
                refreshCurrentUser(result.user)
            } catch (error: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Authentication failed"
                    )
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        refreshCurrentUser(null)
    }

    class Factory(private val auth: FirebaseAuth, private val userRepo: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(auth, userRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
