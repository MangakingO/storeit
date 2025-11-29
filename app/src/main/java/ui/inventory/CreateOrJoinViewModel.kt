package com.example.storeit.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CreateOrJoinState(
    val inventoryName: String = "",
    val inventoryId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinSuccess: Boolean = false
)

class CreateOrJoinViewModel(private val firestore: FirebaseFirestore) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateOrJoinState())
    val uiState: StateFlow<CreateOrJoinState> = _uiState

    fun onInventoryNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(inventoryName = name)
    }

    fun onInventoryIdChanged(id: String) {
        _uiState.value = _uiState.value.copy(inventoryId = id)
    }

    fun createInventory(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userDocRef = firestore.collection("users").document(userId)
                if (!userDocRef.get().await().exists()) {
                    userDocRef.set(mapOf("inventories" to mapOf<String, String>())).await()
                }

                val newInventory = firestore.collection("inventories").add(mapOf("name" to _uiState.value.inventoryName)).await()
                userDocRef.update("inventories.${newInventory.id}", "admin").await()
                _uiState.value = _uiState.value.copy(joinSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun joinInventory(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val inventoryExists = firestore.collection("inventories").document(_uiState.value.inventoryId).get().await().exists()
                if (inventoryExists) {
                    val userDocRef = firestore.collection("users").document(userId)
                    if (!userDocRef.get().await().exists()) {
                        userDocRef.set(mapOf("inventories" to mapOf<String, String>())).await()
                    }
                    userDocRef.update("inventories.${_uiState.value.inventoryId}", "employee").await()
                    _uiState.value = _uiState.value.copy(joinSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Inventory not found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    class Factory(private val firestore: FirebaseFirestore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateOrJoinViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CreateOrJoinViewModel(firestore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
