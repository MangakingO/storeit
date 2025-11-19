package com.example.storeit.ui.inventory

import com.example.storeit.data.model.InventoryItem

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val isEditorOpen: Boolean = false,
    val isSaving: Boolean = false,
    val editorState: ItemEditorState = ItemEditorState(),
    val editorError: String? = null
)

data class ItemEditorState(
    val id: String? = null,
    val name: String = "",
    val quantity: String = "",
    val description: String = "",
    val price: String = "",
    val inStock: Boolean = true,
    val sku: String = "",
    val imageRes: Int? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() && quantity.toIntOrNull() != null && price.isNotBlank()
}
