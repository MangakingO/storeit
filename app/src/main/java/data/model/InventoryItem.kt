package com.example.storeit.data.model

import com.google.firebase.firestore.DocumentId

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
)
