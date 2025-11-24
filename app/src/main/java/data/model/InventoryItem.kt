package com.example.storeit.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

/**
 * Represents a single inventory entry that is stored in Firestore.
 */
data class InventoryItem(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val quantity: Int = 0,
    val inStock: Boolean = true,
    val sku: String? = null,
    val imageRes: Int? = null,
    val description: String? = null,
    val reorderPoint: Int? = null,
    @get:Exclude var hasPendingWrites: Boolean = false
) {
    val needsReorder: Boolean
        get() = reorderPoint != null && quantity < reorderPoint!!
}
