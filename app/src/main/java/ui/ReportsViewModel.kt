package com.example.storeit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.storeit.data.model.ItemChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ReportsUiState(
    val itemChanges: List<ItemChange> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReportsViewModel(private val firestore: FirebaseFirestore) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    fun fetchItemChanges(inventoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val itemChanges = firestore.collection("inventories").document(inventoryId)
                    .collection("item_changes")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(ItemChange::class.java)
                _uiState.value = _uiState.value.copy(itemChanges = itemChanges, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    class Factory(private val firestore: FirebaseFirestore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReportsViewModel(firestore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
