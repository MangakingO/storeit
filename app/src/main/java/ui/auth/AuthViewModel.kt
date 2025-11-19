package com.example.storeit.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.remember

enum class AuthMode { SignIn, Register }

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val mode: AuthMode = AuthMode.SignIn,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: FirebaseUser? = null
)

class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState(currentUser = auth.currentUser))
        private set

    private fun updateState(transform: (AuthUiState) -> AuthUiState) {
        uiState = transform(uiState)
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

    fun refreshCurrentUser(user: FirebaseUser?) = updateState {
        it.copy(currentUser = user, isLoading = false, errorMessage = null)
    }

    fun submitCredentials() {
        val state = uiState
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
                    auth.createUserWithEmailAndPassword(state.email.trim(), state.password).await()
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
        updateState {
            // Reset the state to reflect no logged-in user
            it.copy(
                currentUser = null,
                email = "",
                password = "",
                confirmPassword = ""
            )
        }
    }

    class Factory(private val auth: FirebaseAuth) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(auth) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
