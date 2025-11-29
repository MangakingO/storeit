package com.example.storeit.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.storeit.data.model.InventoryItem
import com.example.storeit.data.InventoryRepository
import com.example.storeit.data.model.ItemChange
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState

    private var listenerRegistration: ListenerRegistration? = null

    // -----------------------------
    // Real-time Firestore Listener
    // -----------------------------
    fun startListening(userId: String) {
        stopListening() // avoid duplicate listeners

        listenerRegistration = repository.listenToItems(userId) { items ->
            _uiState.value = _uiState.value.copy(items = items)
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    // -----------------------------
    // Editor Controls
    // -----------------------------
    fun openNewItem() {
        _uiState.value = _uiState.value.copy(
            isEditorOpen = true,
            editorState = ItemEditorState(),
            editorError = null
        )
    }

    fun editItem(item: InventoryItem) {
        _uiState.value = _uiState.value.copy(
            isEditorOpen = true,
            editorState = ItemEditorState(
                id = item.id,
                name = item.name,
                quantity = item.quantity.toString(),
                description = item.description ?: "",
                price = item.price,
                inStock = item.inStock,
                sku = item.sku ?: "",
                imageRes = item.imageRes,
                reorderPoint = item.reorderPoint?.toString() ?: ""
            ),
            editorError = null
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(isEditorOpen = false)
    }

    fun updateEditorName(name: String) {
        _uiState.value = _uiState.value.copy(
            editorState = _uiState.value.editorState.copy(name = name)
        )
    }

    fun updateEditorQuantity(qty: String) {
        _uiState.value = _uiState.value.copy(
            editorState = _uiState.value.editorState.copy(quantity = qty)
        )
    }

    fun updateEditorDescription(desc: String) {
        _uiState.value = _uiState.value.copy(
            editorState = _uiState.value.editorState.copy(description = desc)
        )
    }

    fun updateEditorPrice(price: String) {
        _uiState.value = _uiState.value.copy(
            editorState = _uiState.value.editorState.copy(price = price)
        )
    }

    fun updateEditorSku(sku: String) {
        _uiState.value = _uiState.value.copy(
            editorState = _uiState.value.editorState.copy(sku = sku)
        )
    }

    fun updateEditorReorderPoint(reorderPoint: String) {
        _uiState.value = _uiState.value.copy(
            editorState = _uiState.value.editorState.copy(reorderPoint = reorderPoint)
        )
    }


    // -----------------------------
    // Save New OR Edited Item
    // -----------------------------
    fun saveEditor(inventoryId: String) {
        val editor = uiState.value.editorState

        if (!editor.isValid) {
            _uiState.value = _uiState.value.copy(
                editorError = "Name, valid quantity, and price required"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val reorderPoint = editor.reorderPoint.toIntOrNull()
                if (editor.id == null) {
                    repository.addItem(
                        inventoryId,
                        editor.name,
                        editor.quantity.toInt(),
                        editor.description.ifBlank { null },
                        editor.price,
                        editor.inStock,
                        editor.sku.ifBlank { null },
                        editor.imageRes,
                        reorderPoint
                    )
                    repository.logItemChange(inventoryId, ItemChange(itemName = editor.name, changeType = "Created"))
                } else {
                    val oldItem = _uiState.value.items.find { it.id == editor.id }

                    repository.updateItem(
                        inventoryId,
                        editor.id,
                        editor.name,
                        editor.quantity.toInt(),
                        editor.description.ifBlank { null },
                        editor.price,
                        editor.inStock,
                        editor.sku.ifBlank { null },
                        editor.imageRes,
                        reorderPoint
                    )

                    if (oldItem != null) {
                        if (oldItem.quantity != editor.quantity.toInt()) {
                            repository.logItemChange(inventoryId, ItemChange(itemId = editor.id, itemName = editor.name, changeType = "Quantity Changed", oldValue = oldItem.quantity.toString(), newValue = editor.quantity))
                        }
                        if (oldItem.name != editor.name) {
                            repository.logItemChange(inventoryId, ItemChange(itemId = editor.id, itemName = editor.name, changeType = "Name Changed", oldValue = oldItem.name, newValue = editor.name))
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isEditorOpen = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    editorError = e.message
                )
            }
        }
    }

    // -----------------------------
    // Delete Item
    // -----------------------------
    fun deleteItem(inventoryId: String, item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteItem(inventoryId, item.id)
            repository.logItemChange(inventoryId, ItemChange(itemId = item.id, itemName = item.name, changeType = "Deleted"))
        }
    }

    // -----------------------------
    // ViewModel Factory
    // -----------------------------
    class Factory(private val repo: InventoryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(repo) as T
        }
    }
}
