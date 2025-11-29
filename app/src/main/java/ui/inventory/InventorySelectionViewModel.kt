package com.example.storeit.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.storeit.data.InventoryRepository
import com.example.storeit.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InventoryInfo(val id: String, val name: String, val role: String)

data class InventorySelectionState(
    val inventories: List<InventoryInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InventorySelectionViewModel(
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventorySelectionState())
    val uiState: StateFlow<InventorySelectionState> = _uiState

    fun loadInventories(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userProfile = userRepository.getUserProfile(userId)
                val inventoryInfoList = userProfile?.inventories?.map { (id, role) ->
                    val inventoryName = inventoryRepository.getInventoryName(id)
                    InventoryInfo(id, inventoryName, role)
                } ?: emptyList()
                _uiState.value = _uiState.value.copy(inventories = inventoryInfoList, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    class Factory(private val userRepo: UserRepository, private val inventoryRepo: InventoryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventorySelectionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InventorySelectionViewModel(userRepo, inventoryRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
